package ru.hse.cppr.routing.dispatchers

import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.sheets.SheetService
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class SheetDispatcher(val service: SheetService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun getActivitySheet(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get sheet for activity with id=$id")
        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(service.getActivitySheet(id, AuthorizationProvider.getDecodedJwt(exchange))))
        )
    }

    fun getProjectSheet(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get sheet for project with id=$id")
        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(service.getProjectSheet(id, AuthorizationProvider.getDecodedJwt(exchange))))
        )

    }

    fun updateGrade(exchange: HttpServerExchange): Unit {
        val projectId = exchange.queryParameters["projectId"]!!.first().let(UUID::fromString)
        val studentId = exchange.queryParameters["studentId"]!!.first().let(UUID::fromString)
        val stageId = exchange.queryParameters["stageId"]!!.first().let(UUID::fromString)
        val gradeType = exchange.queryParameters["gradeType"]!!.first()
        val grade = exchange.queryParameters["grade"]!!.first()

        val gradeInt = if (grade != null && grade.isNotEmpty()) grade.toInt() else null

        log.i("Method: ${service.serviceName} - Update grade")
        formOkResponse(
            exchange, JsonStream.serialize(
                Any.wrap(
                    service.updateStudentGrade(
                        projectId,
                        studentId,
                        stageId,
                        gradeType,
                        gradeInt
                    )
                )
            )
        )
    }

}