package ru.hse.cppr.service.report.base

import arrow.fx.ForIO
import io.undertow.util.BadRequestException
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy
import org.apache.poi.xwpf.usermodel.*
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.reports.ReportFileExtensions
import ru.hse.cppr.representation.enums.reports.ReportHeaders
import ru.hse.cppr.service.crud.formatted.FormattedProjectRequestService
import ru.hse.cppr.service.crud.formatted.FormattedProjectService
import ru.hse.cppr.service.profile.*
import ru.hse.cppr.service.search.SearchProjectsService
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

abstract class BaseDocxReportService: ReportService, KoinComponent {
    override val reportFileFormat: String
        get() = ReportFileExtensions.DOCX.value

    override val reportFormatHeader: String
        get() = ReportHeaders.DOCX_HEADER.value

    protected val provider: TxProvider<ForIO> by inject()
    protected val log: Log by inject() {
        parametersOf(
                CommandLineApplicationRuntime::class
        )
    }


    protected val activityProfileService: ActivityProfileService                        by inject()
    protected val organisationProfileService: OrganisationProfileService                by inject()
    protected val projectProfileService: ProjectProfileService                          by inject()
    protected val projectRequestProfileService: ProjectRequestProfileService            by inject()
    protected val userProfileService: UserProfileService                                by inject()


    protected val projects = Tables.PROJECTS
    protected val users = Tables.USERS
    protected val schemaContent = Tables.SCHEMA_CONTENT
    protected val activityTable = Tables.ACTIVITY
    protected val organisationUser = Tables.ORGANISATION_USER
    protected val organisations = Tables.ORGANISATIONS
    protected val projectStudents = Tables.PROJECT_STUDENTS

    override fun createReport(params: Map<String, Deque<String>>): ByteBuffer? {
        val doc = try {
            createDocxReport(params)
        } catch (e: Exception) {
            createEmptyReport()
        }

        val outputStream = ByteArrayOutputStream()

        doc.write(outputStream)
        outputStream.close()
        doc.close()

        return ByteBuffer.wrap(outputStream.toByteArray())
    }

    protected abstract fun createDocxReport(params: Map<String, Deque<String>>): XWPFDocument


    protected fun getProfile(params: Map<String, Deque<String>>): Map<String, Any?> {
        val id = (params["id"] ?: error("Invalid parameter")).first().let(UUID::fromString)

        return when ((params["schemaType"] ?: error("Invalid parameter")).first()) {
            "student_profile",
            "user_profile" -> userProfileService.getProfile(id)

            "activity" -> activityProfileService.getProfile(id)
            "project" -> projectProfileService.getProfile(id)
            "project_request" -> projectRequestProfileService.getProfile(id)
            "org_profile" -> organisationProfileService.getProfile(id)

            else -> throw BadRequestException("Invalid parameter")
        }
    }

    protected fun getProfileName(profile: Map<String, Any?>): String {
        val nameAttribute = when (profile["schemaType"].toString()) {
            "student_profile",
            "user_profile",
            "activity" -> "name"

            "project",
            "project_request" -> "projectNameRus"

            "org_profile" -> "orgName"

            else -> throw BadRequestException("Invalid parameter")
        }

        return profile[nameAttribute].toString()
    }


    protected fun mergeCellVertically(table: XWPFTable, col: Int, fromRow: Int, toRow: Int) {
        for (rowIndex in fromRow..toRow) {
            val cell = table.getRow(rowIndex).getCell(col)
            val vmerge = CTVMerge.Factory.newInstance()
            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                vmerge.setVal(STMerge.RESTART)
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                vmerge.setVal(STMerge.CONTINUE)
                // and the content should be removed
                for (i in cell.paragraphs.size downTo 1) {
                    cell.removeParagraph(0)
                }
                cell.addParagraph()
            }
            // Try getting the TcPr. Not simply setting an new one every time.
            var tcPr: CTTcPr? = cell.ctTc.tcPr
            if (tcPr == null) {
                tcPr = cell.ctTc.addNewTcPr()
            }
            tcPr!!.vMerge = vmerge
        }
    }

    //merging horizontally by setting grid span instead of using CTHMerge
    protected fun mergeCellHorizontally(table: XWPFTable, row: Int, fromCol: Int, toCol: Int) {
        val cell = table.getRow(row).getCell(fromCol)
        // Try getting the TcPr. Not simply setting an new one every time.
        var tcPr = cell.ctTc.tcPr
        if (tcPr == null) {
            tcPr = cell.ctTc.addNewTcPr()
        }
        // The first merged cell has grid span property set
        if (tcPr!!.isSetGridSpan) {
            tcPr.gridSpan.setVal(BigInteger.valueOf((toCol - fromCol + 1).toLong()))
        } else {
            tcPr.addNewGridSpan().setVal(BigInteger.valueOf((toCol - fromCol + 1).toLong()))
        }
        // Cells which join (merge) the first one, must be removed
        for (colIndex in toCol downTo fromCol + 1) {
            table.getRow(row).ctRow.removeTc(colIndex)
        }
    }

    protected fun setRun(
            run: XWPFRun,
            fontFamily: String,
            fontSize: Int,
            colorRGB: String,
            text: String,
            bold: Boolean,
            addBreak: Boolean
    ) {
        run.fontFamily = fontFamily
        run.fontSize = fontSize
        run.color = colorRGB
        run.setText(text)
        run.isBold = bold
        if (addBreak) run.addBreak()
    }

    protected fun setCellText(
            row: XWPFTableRow,
            cell: Int,
            fontFamily: String,
            fontSize: Int,
            colorRGB: String,
            text: String,
            alignment: ParagraphAlignment,
            width: String,
            bold: Boolean,
            addBreak: Boolean
    ) {

        val tableCell = row.getCell(cell)
        tableCell.setWidth(width)
        val cellParagraph = tableCell.addParagraph()
        cellParagraph.alignment = alignment
        cellParagraph.spacingAfter = 5
        setRun(cellParagraph.createRun(), fontFamily, fontSize, colorRGB, text, bold, addBreak)
    }

    fun setCellColor(row: XWPFTableRow, cell: Int, color: String) {
        val tableCell = row.getCell(cell)
        tableCell.color = color
    }

    private fun createEmptyReport(): XWPFDocument {
        val doc = XWPFDocument()
        val paragraph = doc.createParagraph()
        val run = paragraph.createRun()

        return doc
    }

    protected fun createPageHeader(doc: XWPFDocument, text: String, fontSize: Int = 12, isBold: Boolean = true) {
        val kolontitulParagraph = doc.createHeaderFooterPolicy().createHeader(XWPFHeaderFooterPolicy.DEFAULT).createParagraph()
        kolontitulParagraph.alignment = ParagraphAlignment.CENTER

        val kolontitul = kolontitulParagraph.createRun()
        kolontitul.setText(text)
        kolontitul.fontSize = fontSize
        kolontitul.isBold = isBold
    }
}