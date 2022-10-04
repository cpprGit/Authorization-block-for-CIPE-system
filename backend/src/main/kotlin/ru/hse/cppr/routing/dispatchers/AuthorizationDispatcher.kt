package ru.hse.cppr.routing.dispatchers

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.extensions.io.monad.map
import arrow.fx.fix
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.interfaces.Claim
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.Cookie
import io.undertow.server.handlers.CookieImpl
import jdk.jshell.Snippet.Kind
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.UserFields
import ru.hse.cppr.service.users.UsersService
import ru.hse.cppr.utils.`as`
import ru.hse.cppr.utils.formOkResponse
import ru.hse.cppr.utils.readKeyFromFile
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.nio.file.*
import java.security.*
import java.security.spec.*
import java.util.*
import kotlin.collections.HashMap


class AuthorizationDispatcher(val service: UsersService) : KoinComponent {
    private val provider: TxProvider<ForIO>                               by inject()
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val pubKeyDir : File = File("backend/src/main/resources/public.pem")
    private val privKeyDir : File = File("backend/src/main/resources/private_key.pem")

    private val pubKeyStr = readKeyFromFile(pubKeyDir)
    private val privKeyStr = readKeyFromFile(privKeyDir)

    private val fetchMapValues = { record: Record ->
        mapOf(
            usersTable.ID.name to record[usersTable.ID]?.toString(),
            usersTable.NAME.name to record[usersTable.NAME]?.toString(),
            usersTable.EMAIL.name to record[usersTable.EMAIL]?.toString(),
            usersTable.PASSWORD.name to record[usersTable.PASSWORD]?.toString(),
            UserFields.ROLE.value to record[usersTable.TYPE]?.toString(),
            usersTable.SCHEMA_CONTENT_ID.name to record[usersTable.SCHEMA_CONTENT_ID]?.toString(),
            usersTable.CREATED_BY.name to record[usersTable.CREATED_BY]?.toString(),
            usersTable.CREATED_TIME.name to record[usersTable.CREATED_TIME]?.toString()
        )
    }

    private val usersTable = Tables.USERS

    private val COLUMNS = arrayListOf(
        usersTable.NAME,
        usersTable.PASSWORD,
        usersTable.EMAIL,
        usersTable.TYPE,
        usersTable.SCHEMA_CONTENT_ID,
        usersTable.CREATED_BY,
        usersTable.CREATED_TIME
    )


    fun userAuthorization(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Trying to authorise new user with email=\'${body["email"].`as`<String>()}\'")

//            TODO: Прописать авторизацию пользователя.
            val txResult = provider.tx { configuration ->
                DSL.using(configuration)
                    .select()
                    .from(usersTable)
                    .where(usersTable.EMAIL.eq(body["email"].toString()))
                    .fetchOne()
            }

            val userFound = runBlocking {
                when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                    is Either.Left -> {
                        throw DatabaseException(cb.a.message, cb.a.cause)
                    }
                    is Either.Right -> {
                        return@runBlocking cb.b
                    }
                }
            }

            if (userFound?.getValue(usersTable.PASSWORD) == body["password"].toString()) {
                val headerClaims: MutableMap<String?, Any?> = HashMap<String?, Any?>()
                headerClaims["user_id"] = userFound?.getValue(usersTable.ID)
                headerClaims["role"] = userFound?.getValue(usersTable.TYPE)
                headerClaims["name"] = userFound?.getValue(usersTable.NAME)


/*                //Get publicKey from pem certificate
                val classLoader = ClassLoader.getSystemClassLoader()
                //TODO: get by dependency injection
                val certEnvName = when (AppConfig.getEnvironment()) {
                    "prd" -> "cppw-prd.pem"
                    else -> "dev-cppr.pem"
                }
                val f = CertificateFactory.getInstance("X.509")
                val certificate = f.generateCertificate(classLoader.getResourceAsStream(certEnvName)) as X509Certificate
                val pk = certificate.getPublicKey()*/

                var pubKey : RSAPublicKey? = null
                var privKey : RSAPrivateKey?= null
                try{
                    val pubKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyStr))
                    val kf = KeyFactory.getInstance("RSA")
                    pubKey = kf.generatePublic(pubKeySpec) as RSAPublicKey
                    val privKeySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKeyStr))
                    privKey = kf.generatePrivate(privKeySpec) as RSAPrivateKey
                } catch(e : Exception){
                    log.i("Error " + e.message)
                }

                val alg = Algorithm.RSA256(pubKey, privKey)
                try {
                    val token = JWT.create()
                        .withClaim("user_id", userFound?.getValue(usersTable.ID).toString())
                        .withClaim("role", userFound?.getValue(usersTable.TYPE).toString())
                        .withClaim("name", userFound?.getValue(usersTable.NAME).toString())
                        .withExpiresAt(DateTime.now().plusMinutes(30).toDate())
                        .withIssuer(AppConfig.getAuthDomainUrl())
                        .sign(alg)
                    log.i("Token ready")
                    val cookie = CookieImpl("token", token)
                    cookie.setPath("/")
                    exchange.setResponseCookie(cookie)
                    val fUser = userFound.let { fetchMapValues(it) }
                    formOkResponse(exchange, JsonStream.serialize(com.jsoniter.any.Any.wrap(fUser)))
                    log.i("Method: dispatchSignUp POST - User signed up")
                } catch (exception: JWTCreationException) {
                    log.i("Invalid Signing configuration / Couldn't convert Claims" + exception.message)
                }
                catch (exception : Exception){
                    log.i("Error: " + exception.message)
                }
            }
            else{
                log.i("No such user found")
            }
        }
    }
}