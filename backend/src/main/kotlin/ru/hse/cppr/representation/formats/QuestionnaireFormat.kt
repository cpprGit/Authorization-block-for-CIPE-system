package ru.hse.cppr.representation.formats

import com.jsoniter.any.Any
import ru.hse.cppr.representation.enums.fields.QuestionnaireFields
import java.util.*


class QuestionnaireFormat(val body: Any) {

    fun getMailGroupId(): UUID {
        return UUID.fromString(body[QuestionnaireFields.MAIL_GROUP_ID.value].toString())
    }

    fun getSchemaId(): UUID {
        return UUID.fromString(body[QuestionnaireFields.SCHEMA_ID.value].toString())
    }

}