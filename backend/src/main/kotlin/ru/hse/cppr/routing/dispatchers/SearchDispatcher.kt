package ru.hse.cppr.routing.dispatchers

import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.search.SearchService
import ru.hse.cppr.utils.formOkResponse

class SearchDispatcher(val service: SearchService): KoinComponent {

    private val log: Log        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun search(exchange: HttpServerExchange): Unit {
        log.i("Method: ${service.serviceName} GET - Search")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.search(exchange.queryParameters))))
        log.i("Method: ${service.serviceName} GET - Success")
    }
}