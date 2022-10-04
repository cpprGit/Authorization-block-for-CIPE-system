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
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.student.StudentProjectApplyService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.bodyWithCreatedByField
import ru.hse.cppr.utils.formOkResponse

class StudentProjectApplyDispatcher(val service: StudentProjectApplyService): KoinComponent {

    private val log: Log        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun applyToProject(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} POST")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.applyToProject(body))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }

    fun acceptApplyToProject(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} POST")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.acceptApplyToProject(body))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }

    fun declineApplyToProject(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} POST")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.declineApplyToProject(body))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }

    fun cancelApplyToProject(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} POST")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.cancelApplyToProject(body))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }

    fun cancelAcceptApplyToProject(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} POST")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.cancelAcceptApplyToProject(body))))
            log.i("Method: ${service.serviceName} POST - Success")
        }
    }

}