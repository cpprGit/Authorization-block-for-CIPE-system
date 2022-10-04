package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.profile.ProfileService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class ProfileDispatcher(val service: ProfileService): KoinComponent {

    private val log: Log        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun getProfile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val jwt = AuthorizationProvider.getDecodedJwt(exchange)


        log.i("Method: ${service.serviceName} - Get profile by id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getProfile(id, jwt))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

    fun editProfile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val jwt = AuthorizationProvider.getDecodedJwt(exchange)

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)


            log.i("Method: ${service.serviceName} - Edit profile with id=$id")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.editProfile(id, body, jwt))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }
}