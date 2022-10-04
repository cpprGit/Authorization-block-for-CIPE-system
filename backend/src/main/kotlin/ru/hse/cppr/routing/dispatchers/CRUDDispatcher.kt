package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.utils.bodyWithCreatedByField
import ru.hse.cppr.utils.formCreatedResponse
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class CRUDDispatcher(val service: CRUDService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun post(exchange: HttpServerExchange) {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)
            val bodyWithCreatedBy = bodyWithCreatedByField(body, AuthorizationProvider.getDecodedJwt(exchange))

            log.i("Method: ${service.serviceName} POST")
            formCreatedResponse(exchange, JsonStream.serialize(Any.wrap(service.create(bodyWithCreatedBy))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }


    fun list(exchange: HttpServerExchange): Unit {
        log.i("Method: ${service.serviceName} LIST")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.list())))
        log.i("Method: ${service.serviceName} LIST - Success")
    }

    fun get(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} GET - id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.get(id))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

    fun update(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)
            val bodyWithCreatedBy = bodyWithCreatedByField(body, AuthorizationProvider.getDecodedJwt(exchange))

            log.i("Method: ${service.serviceName} PATCH - id=$id")
            formOkResponse(exchange, service.update(id, bodyWithCreatedBy))
            log.i("Method: ${service.serviceName} PATCH - Success")
        }
    }

    fun delete(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} DELETE - id=$id")
        formOkResponse(exchange, service.delete(id))
        log.i("Method: ${service.serviceName} DELETE - Success")
    }

}