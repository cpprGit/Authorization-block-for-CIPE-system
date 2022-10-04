package ru.hse.cppr.representation.formats

import ru.hse.cppr.representation.enums.fields.MailGroupFields
import ru.hse.cppr.utils.getOptUUID
import java.util.*


class MailGroupFormat(val body: com.jsoniter.any.Any) {

    fun getName(): String {
        return body[MailGroupFields.NAME.value].toString()
    }

    fun getCreatedBy(): UUID? {
        return getOptUUID(MailGroupFields.CREATED_BY.value, body)
    }

    fun getUsers(): com.jsoniter.any.Any {
        return body[MailGroupFields.USERS.value]
    }


}