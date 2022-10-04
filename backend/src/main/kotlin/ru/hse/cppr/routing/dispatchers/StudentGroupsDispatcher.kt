package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.studentgroups.StudentGroupsService
import ru.hse.cppr.utils.formOkResponse


class StudentGroupsDispatcher(val service: StudentGroupsService): KoinComponent {

    private val log: Log by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun updateStudentGroups(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Method: ${service.serviceName} - update student groups")
            formOkResponse(
                    exchange,
                    JsonStream.serialize(Any.wrap(service.updateStudentGroupLists(body)))
            )
        }
    }

    fun getStudentGroups(exchange: HttpServerExchange): Unit {
        log.i("Method: ${service.serviceName} - get student groups")
        formOkResponse(
                exchange,
                JsonStream.serialize(Any.wrap(service.getStudentGroups()))
        )
    }
}