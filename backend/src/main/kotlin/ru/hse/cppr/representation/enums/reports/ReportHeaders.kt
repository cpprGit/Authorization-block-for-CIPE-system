package ru.hse.cppr.representation.enums.reports

enum class ReportHeaders(val value: String) {
    DOCX_HEADER("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX_HEADER("application/vnd.ms-excel"),
}