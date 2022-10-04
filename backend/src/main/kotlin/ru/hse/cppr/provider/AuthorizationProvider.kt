package ru.hse.cppr.provider

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import arrow.syntax.function.curried
import arrow.syntax.function.reverse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import ru.hse.cppr.application.AppConfig
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.utils.formUnauthorizedResponse
import java.io.File
import java.io.FileInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig.getEnvironment
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.BlockedStatus
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.exception.ForbiddenException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.utils.dispatchHandler
import ru.hse.cppr.utils.formForbiddenResponse
import ru.hse.cppr.utils.readKeyFromFile
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*


object AuthorizationProvider: KoinComponent {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val pubKeyDir : File = File("backend/src/main/resources/public.pem")
    private val privKeyDir : File = File("backend/src/main/resources/private_key.pem")

    private val pubKeyStr = readKeyFromFile(pubKeyDir)
    private val privKeyStr = readKeyFromFile(privKeyDir)

    fun sessionWrapper(f: (HttpServerExchange) -> Unit, roles: Array<UserRoles>): HttpHandler {
        return HttpHandler({ exchange: HttpServerExchange, next: HttpHandler ->
            sessionValidate(
                exchange,
                next,
                roles
            )

            dispatchHandler(exchange, next)
        }.reverse().curried()(HttpHandler(f)))
    }

    fun getDecodedJwt(exchange: HttpServerExchange): DecodedJWT {
        val cookies = exchange.requestCookies ?: throw AuthorizationException("Authorization header not present.")
        val token = cookies["token"]?.value ?: throw AuthorizationException("Authorization token not present.")

        try {
            return JWT.decode(token)
        } catch (e: JWTDecodeException) {
            throw AuthorizationException(e.message)
        }
    }

    fun checkUserIsBlocked(token: String) {
        var jwt: DecodedJWT? = null
        try {
            jwt = JWT.decode(token)
        } catch (e: JWTDecodeException) {
            throw AuthorizationException(e.message)
        }

        val id = UUID.fromString(jwt.getClaim(JwtClaims.ID.value).asString())

        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .selectFrom(Tables.USERS)
                .where(Tables.USERS.ID.eq(id))
                .fetchOne()
                .map { it[Tables.USERS.STATUS] } == BlockedStatus.blocked
        }

        val isUserBlocked = runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        if (isUserBlocked) {
            throw AuthorizationException("User is blocked")
        }
    }

    private fun sessionValidate(exchange: HttpServerExchange, next: HttpHandler, roles: Array<UserRoles>) {
        val cookies = exchange.requestCookies ?: throw AuthorizationException("Authorization header not present.")
        val token = cookies["token"]?.value ?: throw AuthorizationException("Authorization token not present.")

        try {
            validateJWT(token)
            validateRoles(token, roles)
            checkUserIsBlocked(token)
        }
        catch (e: Exception) {
            when (e) {
                is AuthorizationException, is ForbiddenException -> throw e
                else -> throw AuthorizationException("Not Authorized")
            }
        }

        next.handleRequest(exchange)
    }

    private fun validateJWT(jwt: String) {
/*
        //Get publicKey from pem certificate
        val classLoader = ClassLoader.getSystemClassLoader()
        //TODO: get by dependency injection
        val certEnvName = when (getEnvironment()) {
            "prd" -> "cppw-prd.pem"
            else -> "dev-cppr.pem"
        }
        val f = CertificateFactory.getInstance("X.509")
        val certificate = f.generateCertificate(classLoader.getResourceAsStream(certEnvName)) as X509Certificate
        val pk = certificate.getPublicKey()
*/
        val pubKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyStr))
        val kf = KeyFactory.getInstance("RSA")
        val pk = kf.generatePublic(pubKeySpec) as RSAPublicKey

        val alg = Algorithm.RSA256(pk as RSAPublicKey?, null)
        //Add verify params. Some claim checks and date params can be added.
        val verifier = JWT.require(alg)
            .withIssuer(AppConfig.getAuthDomainUrl())
            .build()
        verifier.verify(jwt)
        try {
            val token = JWT.decode(jwt)

            if (token.expiresAt.before(DateTime.now().toDate())) {
                throw ForbiddenException("Token has expired.")
            }

        } catch (e: JWTDecodeException) {
            throw AuthorizationException(e.message)
        }
    }


    private fun validateRoles(jwt: String, roles: Array<out UserRoles>) {
        try {
            val token = JWT.decode(jwt)
            val tokenRoleClaim = token.getClaim(JwtClaims.ROLE.value)

            for (role in roles) {
                if (role.value == tokenRoleClaim.asString()) {
                    return
                }
            }

            throw AuthorizationException("Insufficient user role.")

        } catch (e: JWTDecodeException) {
            throw AuthorizationException(e.message)
        }

    }

}