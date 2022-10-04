package ru.hse.cppr.service.report

import arrow.core.Either
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.jooq.impl.DSL
import ru.hse.cppr.data.database_generated.enums.UserType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.reports.ReportNames
import ru.hse.cppr.service.report.base.BaseDocxReportService
import java.util.*

class ProjectsInfoReportService(override val serviceName: String): BaseDocxReportService() {

    override val reportName: String
        get() = ReportNames.PROJECTS_INFO_REPORT.value


    override fun createDocxReport(params: Map<String, Deque<String>>): XWPFDocument {
        val results = getDataFromDatabase()

        val doc = XWPFDocument()
        val table = doc.createTable(results.size + 1, results[0].keys.size)

        val rowOne = table.getRow(0)
        setCellText(rowOne, 0, "Times New Roman", 12, "000000",  "Наименование", ParagraphAlignment.CENTER, "100", true, false)
        setCellColor(rowOne, 0, "DEDEDE")
        setCellText(rowOne, 1, "Times New Roman", 12, "000000",  "Компания", ParagraphAlignment.CENTER, "100", true, false)
        setCellColor(rowOne, 1, "DEDEDE")
        setCellText(rowOne, 2, "Times New Roman", 12, "000000",  "Период", ParagraphAlignment.CENTER, "100", true, false)
        setCellColor(rowOne, 2, "DEDEDE")
        setCellText(rowOne, 3, "Times New Roman", 12, "000000",  "Руководитель", ParagraphAlignment.CENTER, "100", true, false)
        setCellColor(rowOne, 3, "DEDEDE")
        setCellText(rowOne, 4, "Times New Roman", 12, "000000",  "Количество студентов", ParagraphAlignment.CENTER, "100", true, false)
        setCellColor(rowOne, 4, "DEDEDE")

        for ((i, res) in results.withIndex()) {
            setCellText(table.getRow(i + 1), 0, "Times New Roman", 11, "000000", res["Наименование"].toString(), ParagraphAlignment.CENTER, "20%", false, false)
            setCellText(table.getRow(i + 1), 1, "Times New Roman", 11, "000000", res["Компания"].toString(), ParagraphAlignment.CENTER, "20%", false, false)
            setCellText(table.getRow(i + 1), 2, "Times New Roman", 11, "000000", res["Период"].toString(), ParagraphAlignment.CENTER, "20%", false, false)
            setCellText(table.getRow(i + 1), 3, "Times New Roman", 11, "000000", res["Руководитель"].toString(), ParagraphAlignment.CENTER, "20%", false, false)
            setCellText(table.getRow(i + 1), 4, "Times New Roman", 11, "000000", res["Количество студентов"].toString(), ParagraphAlignment.CENTER, "20%", false, false)
        }

        return doc
    }

    private fun getDataFromDatabase(): MutableList<Map<String, Any>> {
        val projectsM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projects)
                .innerJoin(schemaContent)
                .on(projects.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(users)
                .on(projects.LEADER_ID.eq(users.ID))
                .innerJoin(activityTable)
                .on(projects.ACTIVITY_ID.eq(activityTable.ID))
                .innerJoin(organisationUser)
                .on(users.ID.eq(organisationUser.USER_ID))
                .innerJoin(organisations)
                .on(organisations.ID.eq(organisationUser.ORGANISATION_ID))
                .where(users.TYPE.eq(UserType.mentor))
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            //TODO: fix "Период"
                            "Наименование" to record[projects.NAME_RUS],
                            "Компания" to record[organisations.NAME],
                            "Период" to "с 11.2014г. по 04.2015г",
                            "Руководитель" to record[users.NAME],
                            "Количество студентов" to
                                    DSL.using(configuration)
                                        .select(DSL.count())
                                        .from(projectStudents)
                                        .where(projectStudents.PROJECT_ID.eq(record[projects.ID]))
                                        .fetchOneInto(Int::class.java)
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { projectsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }
}