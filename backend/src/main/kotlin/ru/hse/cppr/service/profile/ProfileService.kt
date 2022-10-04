package ru.hse.cppr.service.profile

import com.auth0.jwt.interfaces.DecodedJWT
import ru.hse.cppr.service.Service
import java.util.*

interface ProfileService: Service {

    fun getProfile(id: UUID): Map<String, Any?>

    fun getProfile(id: UUID, jwt: DecodedJWT): Map<String, Any?>

    fun editProfile(id: UUID, body: com.jsoniter.any.Any, jwt: DecodedJWT)
}