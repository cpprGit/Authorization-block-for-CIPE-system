package ru.hse.cppr.exception.handler

import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.ExceptionHandler
import ru.hse.cppr.utils.*

object ExceptionHandlers {

    fun UnrecognizedExceptionHandler(exchange: HttpServerExchange) {
        val throwable = exchange.getAttachment(ExceptionHandler.THROWABLE) as Throwable
        throwable.printStackTrace()
        formInternalServerErrorResponse(exchange)
    }

    fun BadRequestExceptionHandler(exchange: HttpServerExchange) {
        val throwable = exchange.getAttachment(ExceptionHandler.THROWABLE) as Throwable
        throwable.printStackTrace()
        formBadRequestResponse(exchange, throwable.message.toString())
    }

    fun Auth0ExceptionHandler(exchange: HttpServerExchange) {
        val throwable = exchange.getAttachment(ExceptionHandler.THROWABLE) as Throwable
        throwable.printStackTrace()
        formAuth0BadRequestResponse(exchange, throwable.message.toString())
    }

    fun AuthorizationExceptionHandler(exchange: HttpServerExchange) {
        val throwable = exchange.getAttachment(ExceptionHandler.THROWABLE) as Throwable
        throwable.printStackTrace()
        formUnauthorizedResponse(exchange, throwable.message.toString())
    }

    fun ForbiddenExceptionHandler(exchange: HttpServerExchange) {
        val throwable = exchange.getAttachment(ExceptionHandler.THROWABLE) as Throwable
        throwable.printStackTrace()
        formForbiddenResponse(exchange, throwable.message.toString())
    }

}