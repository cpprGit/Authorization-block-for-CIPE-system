package ru.hse.cppr.routing

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.FormattedService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formOkResponse

object CurrentSchemaDispatcher: KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun getFormatted(exchange: HttpServerExchange): Unit {
        val type = exchange.queryParameters["type"]!!.first()

        log.i("Method: DefaultFormDispathcer GET - id=$type")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(CurrentSchemaService.getFormatted(SchemaType.valueOf(type)))))
        log.i("Method: DefaultFormDispathcer GET - Success")
    }

    fun get(exchange: HttpServerExchange): Unit {
        val type = exchange.queryParameters["type"]!!.first()

        log.i("Method: DefaultFormDispathcer GET - id=$type")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(CurrentSchemaService.get(SchemaType.valueOf(type)))))
        log.i("Method: DefaultFormDispathcer GET - Success")
    }

    fun patch(exchange: HttpServerExchange): Unit {
        val type = exchange.queryParameters["type"]!!.first()
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)


            log.i("Method: DefaultFormDispathcer PATCH - id=$type")
            formOkResponse(exchange, JsonStream.serialize(Any.wrap(CurrentSchemaService.update(SchemaType.valueOf(type), body))))
            log.i("Method: DefaultFormDispathcer PATCH - Success")
        }
    }
}