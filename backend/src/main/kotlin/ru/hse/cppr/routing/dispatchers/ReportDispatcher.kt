package ru.hse.cppr.routing.dispatchers

import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.report.base.ReportService
import ru.hse.cppr.utils.header
import ru.hse.cppr.utils.status

class ReportDispatcher(val service: ReportService): KoinComponent {

    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun get(exchange: HttpServerExchange): Unit {
        exchange.status(StatusCodes.OK)
        exchange.header("Access-Control-Allow-Origin", "*")
        exchange.header("Content-Type", service.reportFormatHeader)
        exchange.header("Content-Disposition", "attachment; filename=\"${service.reportName}${service.reportFileFormat}\"")

        log.i("Method: ${service.serviceName} GET - Forming report: ${service.reportName}${service.reportFileFormat}")
        exchange.responseSender.send(service.createReport(exchange.queryParameters))
        log.i("Method: ${service.serviceName} GET - Success")

    }
}