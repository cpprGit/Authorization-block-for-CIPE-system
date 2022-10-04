package ru.hse.cppr.routing

import arrow.fx.ForIO
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.status

object ErrorDispatcher : KoinComponent {
    private val provider: TxProvider<ForIO>                               by inject()

    fun get(exchange: HttpServerExchange): Unit {
        exchange.status(200)
            .body("Unauthorized example")
            .endExchange()
    }
}