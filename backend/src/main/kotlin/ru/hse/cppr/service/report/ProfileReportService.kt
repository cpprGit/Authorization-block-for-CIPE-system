package ru.hse.cppr.service.report

import com.jsoniter.JsonIterator
import io.undertow.util.BadRequestException
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.reports.ReportNames
import ru.hse.cppr.service.report.base.BaseDocxReportService
import java.util.*

class ProfileReportService(override val serviceName: String): BaseDocxReportService() {

    override val reportName: String
        get() = profileReportName

    var profileReportName = ReportNames.PROFILE_REPORT.value


    override fun createDocxReport(params: Map<String, Deque<String>>): XWPFDocument {
        val profile = getProfile(params)
        val report = getReportInfo(profile)

        val profileName = getProfileName(profile)
        renameReportFile(profile["schemaType"].toString())

        val doc = XWPFDocument()

        createPageHeader(doc, "Описание \"$profileName\" ФКН НИУ ВШЭ")
        createTableInfo(doc, report, "Информация о \"$profileName\"")

        return doc
    }


    private fun createTableInfo(doc: XWPFDocument, report: LinkedHashMap<String, String>, headerText: String = "Информация"): XWPFTable {
        val table = doc.createTable(report.size + 1, 2)
        table.styleID = "Normal"
        table.setCellMargins(50, 100, 50, 100)
        table.setWidth("98%")


        mergeCellHorizontally(table, 0, 0, 1)
        val headerCell = table.getRow(0).getCell(0)
        headerCell.verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER

        val headerParagraph = headerCell.paragraphs[0]
        headerParagraph.alignment = ParagraphAlignment.CENTER
        headerParagraph.spacingAfter = 0

        val header = headerParagraph.createRun()
        header.setText(headerText)
        header.isBold = true


        for ((i, key) in report.keys.withIndex()) {
            val cell0 = table.getRow(i + 1).getCell(0)
            cell0.text = key
            cell0.setWidth("30%")
            cell0.paragraphs[0].spacingAfter = 0

            val cell1 = table.getRow(i + 1).getCell(1)
            cell1.text = report[key].toString()
            cell1.verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
            cell1.paragraphs[0].spacingAfter = 0
        }

        return table
    }

    private fun getReportInfo(profile: Map<String, Any?>): LinkedHashMap<String, String> {
        val report = LinkedHashMap<String, String>()

        val fields = profile["fields"] as List<Map<String, String>>

        for (field in fields) {
            val person = profile[field["name"]] as? Map<String, String>

            if (person != null)
                report[field["title"].toString()] = person["name"].toString()
            else
                report[field["title"].toString()] = profile[field["name"]].toString()
        }

        val attributes = profile["attributes"] as List<Map<String, String>>

        try {
            val contentJson = JsonIterator.deserialize(profile[CommonFields.SCHEMA_CONTENT.value].toString())

            for (attribute in attributes) {
                val person = profile[attribute["name"]] as? Map<String, String>

                if (person != null)
                    report[attribute["title"].toString()] = person["name"].toString()
                else
                    report[attribute["title"].toString()] = contentJson[attribute["name"]].toString()
            }

        } catch (e: Exception) {
            log.e(e.message.toString())
        }

        return report
    }


    private fun renameReportFile(schemaType: String) {
        profileReportName = if (schemaType.endsWith("_profile"))
            schemaType + "_report"
        else
            schemaType + "_profile_report"
    }
}