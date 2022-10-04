package ru.hse.cppr.routing

import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.StatusCodes
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig.getAuthCallbackUrl
import ru.hse.cppr.application.AppConfig.getAuthClientId
import ru.hse.cppr.application.AppConfig.getAuthDomain
import ru.hse.cppr.application.AppConfig.getServerHostPort
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.UriBuilder
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.status


object LoginDispatcher : KoinComponent {
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    fun post(exchange: HttpServerExchange): Unit {
        redirectToAuth0(exchange.queryParameters["state"]?.first.toString(), exchange)
    }

    private fun redirectToAuth0(state : String, exchange: HttpServerExchange) {
        exchange.status(StatusCodes.FOUND)
        exchange.responseHeaders.put(Headers.LOCATION, buildAuth0Url(state))
        exchange.endExchange()
    }

    private fun buildAuth0Url(state : String) : String {
        return UriBuilder(getAuthDomain())
            .setConnectionType("https")
            .addSubfolder("authorize")
            .addParameter("response_type", "code")
            .addParameter("client_id", getAuthClientId())
            .addParameter("redirect_uri", getAuthCallbackUrl())
            .addParameter("scope", "openid email profile")
            .addParameter("audience", "http://" + getServerHostPort())
            .addParameter("state", state)
            .url
    }
}

