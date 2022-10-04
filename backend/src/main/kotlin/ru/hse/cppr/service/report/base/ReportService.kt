package ru.hse.cppr.service.report.base

import ru.hse.cppr.service.Service
import java.nio.ByteBuffer
import java.util.*

interface ReportService: Service {

    val reportName: String

    val reportFileFormat: String

    val reportFormatHeader: String

    fun createReport(params: Map<String, Deque<String>>): ByteBuffer?
}