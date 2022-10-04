package ru.hse.cppr.routing.dispatchers

import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class NotificationsDispatcher(val service: NotificationsService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun getNotification(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get notification with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getNotification(id))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

    fun getNotificationsFor(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get notifications for id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getNotificationsFor(id))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

    fun unreadNotification(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Unread notification with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.unreadNotification(id))))
        log.i("Method: ${service.serviceName} POST - Success")
    }
}