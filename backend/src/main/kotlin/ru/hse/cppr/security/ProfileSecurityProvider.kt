package ru.hse.cppr.security

import com.auth0.jwt.interfaces.DecodedJWT
import org.jooq.Configuration
import org.koin.standalone.KoinComponent
import java.util.*

interface ProfileSecurityProvider: KoinComponent {

    fun isModifyAllowed(id: UUID, jwt: DecodedJWT): Boolean

    fun checkModifyAllowed(id: UUID, jwt: DecodedJWT)

}