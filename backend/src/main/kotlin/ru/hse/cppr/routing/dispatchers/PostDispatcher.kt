package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.security.PostsSecurityProvider
import ru.hse.cppr.security.UserProfileSecurityProvider
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.bodyWithCreatedByField
import ru.hse.cppr.utils.formCreatedResponse
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class PostDispatcher(val service: PostsService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun createPost(exchange: HttpServerExchange): Unit {
        val profileId = exchange.queryParameters["profileId"]!!.first().let(UUID::fromString)
        val jwt = AuthorizationProvider.getDecodedJwt(exchange)

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            if (profileId == null) {
                throw AuthorizationException("Access Not Allowed")
            }

            log.i("Method: ${service.serviceName} - Create post")
            formCreatedResponse(exchange, JsonStream.serialize(Any.wrap(service.createPost(profileId, body, jwt))))
        }
    }

    fun updatePost(exchange: HttpServerExchange): Unit {
        val postId = exchange.queryParameters["postId"]!!.first().let(UUID::fromString)
        val jwt = AuthorizationProvider.getDecodedJwt(exchange)

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            if (postId == null) {
                throw AuthorizationException("Access Not Allowed")
            }


            log.i("Method: ${service.serviceName} - Create post")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.updatePost(postId, body, jwt))))
        }
    }

    fun getPostsForProfile(exchange: HttpServerExchange): Unit {
        val profileId = exchange.queryParameters["profileId"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get posts for profile id=$profileId")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getPostsByProfileId(profileId))))
    }

    fun deletePost(exchange: HttpServerExchange): Unit {
        val postId = exchange.queryParameters["postId"]!!.first().let(UUID::fromString)
        val jwt = AuthorizationProvider.getDecodedJwt(exchange)


        if (postId == null) {
            throw AuthorizationException("Access Not Allowed")
        }

        log.i("Method: ${service.serviceName} - Delete post with id=$postId")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.deletePost(postId, jwt))))
    }
}