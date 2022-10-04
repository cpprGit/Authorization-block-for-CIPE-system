package ru.hse.cppr.service.report.base

import arrow.fx.ForIO
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.*
import ru.hse.cppr.representation.enums.reports.ReportFileExtensions
import ru.hse.cppr.representation.enums.reports.ReportHeaders
import ru.hse.cppr.service.crud.formatted.FormattedProjectService
import ru.hse.cppr.service.search.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*

abstract class BaseXlsxReportService: ReportService, KoinComponent {
    override val reportFileFormat: String
        get() = ReportFileExtensions.XLSX.value

    override val reportFormatHeader: String
        get() = ReportHeaders.XLSX_HEADER.value

    protected val provider: TxProvider<ForIO>                                                     by inject()
    protected val log: Log                                                                        by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }


    protected val searchProjectsService: SearchProjectsService                                    by inject()
    protected val searchProjectRequestsService: SearchProjectRequestsService                      by inject()
    protected val searchStudentsService: SearchStudentsService                                    by inject()
    protected val searchOrganisationsService: SearchOrganisationsService                          by inject()
    protected val searchUsersService: SearchUsersService                                          by inject()
    protected val searchActivitiesService: SearchActivitiesService                                by inject()
    protected val searchQuestionnairesService: SearchQuestionnairesService                        by inject()

    protected val projects = Tables.PROJECTS
    protected val users = Tables.USERS
    protected val schemaContent = Tables.SCHEMA_CONTENT
    protected val activityTable = Tables.ACTIVITY
    protected val organisationUser = Tables.ORGANISATION_USER
    protected val organisations = Tables.ORGANISATIONS
    protected val projectStudents = Tables.PROJECT_STUDENTS

    override fun createReport(params: Map<String, Deque<String>>): ByteBuffer? {
        val doc = try {
            createXlsxReport(params)
        } catch (e: Exception) {
            createEmptyReport()
        }

        val outputStream = ByteArrayOutputStream()

        doc.write(outputStream)
        outputStream.close()
        doc.close()

        return ByteBuffer.wrap(outputStream.toByteArray())
    }

    protected abstract fun createXlsxReport(params: Map<String, Deque<String>>): XSSFWorkbook

    protected fun parseFieldValue(key: String, value: String): String {
        return when (key) {
            ProjectFields.PROJECT_NAME_RUS.value,
            ProjectFields.PROJECT_NAME_ENG.value,
            ProjectFields.MENTOR.value,
            ProjectFields.CONSULTANT.value,
            CommonFields.NAME.value,
            OrganisationFields.NAME.value,
            OrganisationFields.PARENT.value,
            MentorFields.ORGANISATION.value,
            ProjectFields.ACTIVITY.value -> {
                try {
                    val map = value.trim('{').trim('}').split(",").associate {
                        val (left, right) = it.trim(' ').split("=")
                        left to right
                    }
                    map[CommonFields.NAME.value].toString()
                } catch (e: Exception) {
                    ""
                }
            }
            OrganisationFields.IS_HSE_DEPARTMENT.value -> {
                val hseDep =
                    when (value) {
                        "true" -> "да"
                        "false" -> "нет"
                        else -> try {
                            val map = value.trim('{').trim('}').split(",").associate {
                                val (left, right) = it.trim(' ').split("=")
                                left to right
                            }
                            map[CommonFields.NAME.value].toString()
                        } catch (e: Exception) {
                            ""
                        }
                    }

                hseDep
            }
            else -> value
        }
    }

    private fun createEmptyReport(): XSSFWorkbook {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Projects")
        val titles = sheet.createRow(0)
        return wb
    }


}