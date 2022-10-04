package ru.hse.cppr.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.StatusCodes
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig.getAuthCallbackUrl
import ru.hse.cppr.application.AppConfig.getAuthClientId
import ru.hse.cppr.application.AppConfig.getAuthClientSecret
import ru.hse.cppr.application.AppConfig.getAuthTokenUrl
import ru.hse.cppr.application.AppConfig.getServerHostPort
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.UriBuilder
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.utils.*


object CallbackDispatcher : KoinComponent {
    private val log: Log                                                  by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val client: OkHttpClient                                      by inject()

    //TODO: add logging
    fun get(exchange: HttpServerExchange): Unit {
        val code = exchange.queryParameters["code"]?.first().toString()
        val state = exchange.queryParameters["state"]?.first().toString()
//        val cookies = exchange.requestCookies ?: throw AuthorizationException("Authorization header not present.")
//        val token = cookies["token"]?.value ?: throw AuthorizationException("Authorization token not present.")

        val response = client.newCall(formRequestToAuth0(code)).execute()
        val responseBody = response.body?.string()

        val responseJsonObject = JsonParser().parse(responseBody).asJsonObject

        //TODO: replace with proper exception
        if (response.code != StatusCodes.OK) {
            redirectToErrorUrl(exchange, responseBody)
            return
        }

//        AuthorizationProvider.checkUserIsBlocked(token)

        redirectToCallbackUrl(exchange, responseJsonObject, state)
    }

    private fun redirectToCallbackUrl(exchange: HttpServerExchange, responseJsonObject: JsonObject, state : String) {
        exchange.status(StatusCodes.FOUND)
        exchange.responseHeaders.put(Headers.LOCATION, buildRedirectUri(responseJsonObject, state))
        exchange.endExchange()
    }

    private fun redirectToErrorUrl(exchange: HttpServerExchange, responseBody : String?) {
        exchange.status(StatusCodes.FOUND)
        exchange.responseHeaders.put(Headers.LOCATION, "/api/v1/error")
        exchange.body(responseBody.toString())
        exchange.endExchange()
    }

    private fun buildRedirectUri(responseJSONObject : JsonObject, state : String) : String {
        return UriBuilder(getServerHostPort())
            .setConnectionType("http")
            .addPathResource(state)
            .addParameter("token", responseJSONObject.get("id_token").asString).url
    }

    private fun formRequestToAuth0(code : String) : Request {
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = (
                    "grant_type=authorization_code&" +
                    "client_id=${getAuthClientId()}&" +
                    "client_secret=${getAuthClientSecret()}&" +
                    "redirect_uri=${getAuthCallbackUrl()}&" +
                    "code=$code"
                ).toRequestBody(mediaType)

        return Request.Builder()
            .url(getAuthTokenUrl())
            .post(body)
            .addHeader("cache-control", "no-cache")
            .build()
    }
}