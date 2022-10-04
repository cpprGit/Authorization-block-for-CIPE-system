package ru.hse.cppr.security

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.BlockedStatus
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.UserRoles
import java.util.*

class OrganisationProfileSecurityProvider: ProfileSecurityProvider {

    private val organisationUser = Tables.ORGANISATION_USER
    private val organisationsTable = Tables.ORGANISATIONS

    val provider: TxProvider<ForIO>     by inject()

    override fun isModifyAllowed(id: UUID, jwt: DecodedJWT): Boolean {
        val tx = provider.tx { configuration ->

            val requesterId = UUID.fromString(jwt.getClaim(JwtClaims.ID.value).asString())

            when (jwt.getClaim(JwtClaims.ROLE.value).asString()) {
                UserRoles.SUPERVISOR.value,
                UserRoles.ADMIN.value,
                UserRoles.MANAGER.value -> true
                UserRoles.STUDENT.value -> false
                else -> {
                    DSL.using(configuration)
                        .select()
                        .from(organisationUser)
                        .innerJoin(organisationsTable)
                        .on(organisationsTable.ID.eq(organisationUser.ORGANISATION_ID))
                        .where(
                            organisationUser.ORGANISATION_ID.eq(id)
                                .and(
                                    organisationUser.USER_ID.eq(requesterId)
                                        .and(organisationsTable.BLOCKED_STATUS.eq(BlockedStatus.active))
                                )
                        )
                        .fetchAny() != null
                }
            }
        }

        return runBlocking {
            when (val cb = Either.catch { tx.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun checkModifyAllowed(id: UUID, jwt: DecodedJWT) {
        if (!isModifyAllowed(id, jwt)) {
            throw AuthorizationException("Action not allowed")
        }
    }
}