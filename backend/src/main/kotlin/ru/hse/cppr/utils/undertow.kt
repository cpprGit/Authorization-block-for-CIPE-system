package ru.hse.cppr.utils

import arrow.core.Option
import arrow.core.toOption
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object UndertowErrors {

    val NOT_FOUND_JSON = Any.wrap(mapOf("message" to "Requested resource could not be found"))
    val NOT_FOUND      = JsonStream.serialize(NOT_FOUND_JSON)
    val TIMEOUT_JSON   = Any.wrap(mapOf("message" to "Request timed out"))
    val TIMEOUT        = JsonStream.serialize(TIMEOUT_JSON)
    val INTERNAL_JSON  = Any.wrap(mapOf("message" to "Internal server error"))
    val INTERNAL       = JsonStream.serialize(INTERNAL_JSON)
    val PARSING_JSON   = Any.wrap(mapOf("message" to "Parsing error"))
    val PARSING        = JsonStream.serialize(PARSING_JSON)
    val BAD_JSON       = Any.wrap(mapOf("message" to "Bad request"))
    val BAD            = JsonStream.serialize(BAD_JSON)
}

fun HttpServerExchange.header(name: String)
        = requestHeaders[name].toOption()
    .map { it.first }

fun HttpServerExchange.path(name: String): Option<String>
        = pathParameters[name].toOption()
    .map { it.first }

fun HttpServerExchange.param(name: String): Option<String>
        = queryParameters[name].toOption()
    .map { it.first }

suspend fun HttpServerExchange.body(): String
        = suspendCoroutine { continuation ->
    requestReceiver.receiveFullBytes { _, body ->
        try {
            val text = String(body, charset("utf-8"))
            continuation.resume(text)
        } catch (e: Exception) {
            println("Receive body error")
            continuation.resumeWith(Result.failure(e))
        }
    }
}


fun dispatchHandler(exchange: HttpServerExchange, handler: HttpHandler): Unit {
    if (exchange.isInIoThread) {
        exchange.dispatch(handler)
        return
    }
}



suspend fun HttpServerExchange.byteBody(): ByteArray
        = suspendCoroutine { continuation ->
    requestReceiver.receiveFullBytes { _, body ->
        continuation.resume(body)
    }
}

fun HttpServerExchange.status(status: Int)
        = apply { statusCode = status }

fun HttpServerExchange.header(name: String, value: String)
        = apply { responseHeaders.add(HttpString.tryFromString(name), value) }

fun HttpServerExchange.body(data: String)
        = apply { responseSender.send(data) }
