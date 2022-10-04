package ru.hse.cppr.security

import com.auth0.jwt.interfaces.DecodedJWT
import org.jooq.Configuration
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.representation.enums.UserRoles
import java.util.*

class ActivityProfileSecurityProvider: ProfileSecurityProvider {

    override fun checkModifyAllowed(id: UUID, jwt: DecodedJWT) {
        if (!isModifyAllowed(id, jwt)) {
            throw AuthorizationException("Action not allowed")
        }
    }

    override fun isModifyAllowed(id: UUID, jwt: DecodedJWT): Boolean {
        return when (jwt.getClaim(JwtClaims.ROLE.value).asString()) {
            UserRoles.SUPERVISOR.value,
            UserRoles.ADMIN.value,
            UserRoles.MANAGER.value -> true
            else ->  false
        }
    }

}