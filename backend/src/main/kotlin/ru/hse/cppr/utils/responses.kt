package ru.hse.cppr.utils

import com.jsoniter.JsonIterator
import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes
import kotlinx.coroutines.runBlocking



fun formOkResponse(exchange: HttpServerExchange, responseBody : String) {
    val allowedOrigin = exchange.requestHeaders["Origin"]?.firstOrNull()

    exchange.status(StatusCodes.OK)
        .header("Content-Type", "application/json;charset=utf-8")
        .header("Allow", "OPTIONS")
        .header("Access-Control-Allow-Origin", "$allowedOrigin")
        .header("Access-Control-Allow-Methods", "DELETE, PATCH, POST, GET, OPTIONS")
        .header("Access-Control-Allow-Headers", "Content-Type")
        .header("Access-Control-Allow-Credentials", "true")
        .body(responseBody)
        .endExchange()
}

fun formCreatedResponse(exchange: HttpServerExchange, responseBody : String) {
    val allowedOrigin = exchange.requestHeaders["Origin"]?.firstOrNull()

    exchange.status(StatusCodes.CREATED)
        .header("Content-Type", "application/json;charset=utf-8")
        .header("Access-Control-Allow-Origin", "$allowedOrigin")
        .header("Access-Control-Allow-Credentials", "true")
        .body(responseBody)
        .endExchange()
}

fun formBadRequestResponse(exchange: HttpServerExchange, reasonPhrase : String) {
    exchange.status(StatusCodes.BAD_REQUEST)
        .header("Content-Type", "application/json;charset=utf-8")
        .body(formJsonResponse("Error", reasonPhrase))
        .endExchange()
}

fun formUnauthorizedResponse(exchange: HttpServerExchange, reasonPhrase : String) {
    exchange.status(StatusCodes.UNAUTHORIZED)
        .header("Content-Type", "application/json;charset=utf-8")
        .body(formJsonResponse("Error", reasonPhrase))
        .endExchange()
}

fun formForbiddenResponse(exchange: HttpServerExchange, reasonPhrase : String) {
    exchange.status(StatusCodes.FORBIDDEN)
        .header("Content-Type", "application/json;charset=utf-8")
        .body(formJsonResponse("Error", reasonPhrase))
        .endExchange()
}

fun formUnauthorizedResponse(exchange: HttpServerExchange) {
    exchange.status(StatusCodes.UNAUTHORIZED)
        .body(formJsonResponse("Error", "Not Authorized"))
        .endExchange()
}

fun formAuth0BadRequestResponse(exchange: HttpServerExchange, reasonPhrase : String) {
    val body = runBlocking { JsonIterator.deserialize(reasonPhrase) }

    val statusCode = try {
        body?.get("statusCode")?.toInt() ?: StatusCodes.BAD_REQUEST
    } catch (e: Exception) {
        StatusCodes.BAD_REQUEST
    }

    exchange.status(statusCode)
        .header("Content-Type", "application/json;charset=utf-8")
        .body(reasonPhrase)
        .endExchange()
}

fun formInternalServerErrorResponse(exchange: HttpServerExchange) {
    exchange.status(StatusCodes.INTERNAL_SERVER_ERROR)
        .body("{\"message\": \"Internal server error\"}")
        .endExchange()
}