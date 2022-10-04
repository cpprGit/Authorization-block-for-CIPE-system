package ru.hse.cppr.service.report

import com.jsoniter.JsonIterator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.hse.cppr.representation.enums.fields.AttributeFields
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.reports.ReportNames
import ru.hse.cppr.service.report.base.BaseXlsxReportService
import java.net.URLDecoder
import java.util.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFCellStyle


class ProjectsSearchReportService(override val serviceName: String): BaseXlsxReportService() {

    override val reportName: String
        get() = ReportNames.PROJECTS_SEARCH_REPORT.value


    override fun createXlsxReport(params: Map<String, Deque<String>>): XSSFWorkbook {
        val reportColumns = URLDecoder.decode(params[CommonFields.FIELDS.value]?.first, "UTF-8").split(',')

        val searchResult = searchStudentsService.search(params)

        val schema = searchResult?.get(CommonFields.SCHEMA.value) as Map<String, Any?>
        val records = searchResult[CommonFields.RECORDS.value] as List<Map<String, Any?>>

        val fields = schema[CommonFields.FIELDS.value] as List<Map<String, Any?>>
        val attributes = schema[CommonFields.ATTRIBUTES.value] as List<Map<String, Any?>>

        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Projects")
        val titles = sheet.createRow(0)

        var tempEntity = mutableMapOf<String, String>()

        val headerStyle = createHeaderStyle(wb)
        val cellStyle = createBorderedStyle(wb)


        for ((rowIndex, record) in records.withIndex()) {
            val row = sheet.createRow(rowIndex + 1)

            val entity = mutableMapOf<String, String>()

            for (field in fields) {
                if (reportColumns.contains(field[CommonFields.NAME.value].toString())) {
                    val key = field[CommonFields.TITLE.value].toString()
                    val value = record[field[CommonFields.NAME.value].toString()].toString().replace("null", "")
                    entity[key] = parseFieldValue(field[CommonFields.NAME.value].toString(), value)
                }
            }

            for (attribute in attributes) {
                if (reportColumns.contains(attribute[AttributeFields.NAME.value].toString())) {
                    try {
                        val contentJson = JsonIterator.deserialize(record[CommonFields.SCHEMA_CONTENT.value].toString())
                        entity[attribute[AttributeFields.NAME.value].toString()] =
                            contentJson[attribute[AttributeFields.NAME.value]].toString()
                    } catch (e: Exception) {
                        log.w("No content for attribute: ${attribute[AttributeFields.NAME.value]}")
                        entity[attribute[AttributeFields.NAME.value].toString()] = ""
                    }
                }
            }

            for ((cellIndex, key) in entity.keys.withIndex()) {
                val cell = row.createCell(cellIndex)
                cell.cellStyle = cellStyle as XSSFCellStyle?
                cell.setCellValue(entity[key])
            }

            tempEntity = entity
        }



        for ((cellIndex, key) in tempEntity.keys.withIndex()) {
            val titleCell = titles.createCell(cellIndex)

            titleCell.cellStyle = headerStyle as XSSFCellStyle?
            titleCell.setCellValue(key)
            sheet.autoSizeColumn(cellIndex)
        }

        return wb
    }

    private fun createBorderedStyle(wb: Workbook): CellStyle {
        val style = wb.createCellStyle()
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.rightBorderColor = IndexedColors.BLACK.getIndex()
        style.bottomBorderColor = IndexedColors.BLACK.getIndex()
        style.leftBorderColor = IndexedColors.BLACK.getIndex()
        style.topBorderColor = IndexedColors.BLACK.getIndex()
        return style
    }

    private fun createHeaderStyle(wb: Workbook): CellStyle {
        val headerStyle: CellStyle
        val headerFont = wb.createFont()
        headerFont.bold = true
        headerFont.fontHeightInPoints = 12.toShort()
        headerStyle = createBorderedStyle(wb)
        headerStyle.alignment = HorizontalAlignment.CENTER
        headerStyle.setFont(headerFont)
        headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        headerStyle.setFont(headerFont)

        return headerStyle
    }
}