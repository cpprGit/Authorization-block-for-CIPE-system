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
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.bodyWithCreatedByField
import ru.hse.cppr.utils.formCreatedResponse
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class MailGroupHistoryDispatcher(val service: MailGroupHistoryService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun createRecord(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} Create Record for mail group with id=$id")
            formCreatedResponse(exchange, JsonStream.serialize(Any.wrap(service.createRecord(id, body))))
            log.i("Method: ${service.serviceName} Create Record - Success")
        }

    }

    fun createRecords(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} Create Records for mail group with id=$id")
            formCreatedResponse(exchange, JsonStream.serialize(Any.wrap(service.createRecords(id, body))))
            log.i("Method: ${service.serviceName} Create Record - Success")
        }
    }


    fun getHistory(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} Get history for mail group with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getRecords(id))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

}