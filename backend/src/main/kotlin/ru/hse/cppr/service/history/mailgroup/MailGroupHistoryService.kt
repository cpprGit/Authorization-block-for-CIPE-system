package ru.hse.cppr.service.history.mailgroup

import org.jooq.Configuration
import ru.hse.cppr.representation.formats.MailGroupHistoryFormat
import ru.hse.cppr.service.Service
import java.util.*

interface MailGroupHistoryService: Service {
    fun createRecord(mailGroupId: UUID, body: com.jsoniter.any.Any): String

    fun createRecords(mailGroupId: UUID, body: com.jsoniter.any.Any): String

    fun getRecords(mailGroupId: UUID): MutableList<Map<String, Any?>>

    fun createRecordProgram(mailGroupId: UUID, bodyFormat: MailGroupHistoryFormat, configuration: Configuration): Map<String, Any?>

    fun createMailGroupHistoryRecord(id: UUID, body: com.jsoniter.any.Any, targetType: String, configuration: Configuration): MutableList<Map<String, Any>>
}