package ru.hse.cppr.representation.formats

import org.koin.core.parameter.parametersOf
import ru.hse.cppr.representation.enums.fields.PostFields
import ru.hse.cppr.utils.asOpt
import ru.hse.cppr.utils.getOpt
import ru.hse.cppr.utils.getOptUUID
import java.util.*


class PostFormat(val body: com.jsoniter.any.Any) {

    fun getCreatedBy(): UUID {
        return UUID.fromString(body[PostFields.CREATED_BY.value].toString())
    }

    fun getMessage(): String {
        return body[PostFields.MESSAGE.value].toString()
    }

    fun getText(): String? {
        return body.getOpt(PostFields.TEXT.value)?.asOpt()
    }

    fun getFile(): UUID? {
        return getOptUUID(PostFields.FILE.value, body)
    }

}