package ru.hse.cppr.service.posts

import com.auth0.jwt.interfaces.DecodedJWT
import org.jooq.Configuration
import ru.hse.cppr.service.Service
import java.util.*

interface PostsService : Service {

    fun createPost(profileId: UUID, body: com.jsoniter.any.Any, jwt: DecodedJWT): Map<String, Any?>

    fun getPostsByProfileId(id: UUID): MutableList<Map<String, Any?>>

    fun getPostsByProfileIdProgram(id: UUID, configuration: Configuration): MutableList<Map<String, Any?>>

    fun updatePost(id: UUID, body: com.jsoniter.any.Any, jwt: DecodedJWT): String

    fun deletePost(id: UUID, jwt: DecodedJWT): String
}