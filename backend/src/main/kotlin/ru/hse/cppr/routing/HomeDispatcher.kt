package ru.hse.cppr.routing

import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formJsonResponse
import ru.hse.cppr.utils.status

object HomeDispatcher : KoinComponent {
    private val log: Log                                                  by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun get(exchange: HttpServerExchange): Unit {
        exchange.status(200)
            .body(formJsonResponse("home", "Home!"))
            .endExchange()
    }

}