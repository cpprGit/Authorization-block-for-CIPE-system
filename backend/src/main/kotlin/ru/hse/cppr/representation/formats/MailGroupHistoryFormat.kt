package ru.hse.cppr.representation.formats

import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.MailGroupFields
import ru.hse.cppr.representation.enums.fields.MailGroupHistoryFields
import ru.hse.cppr.utils.asOpt
import ru.hse.cppr.utils.getOpt
import ru.hse.cppr.utils.getOptUUID
import java.util.*


class MailGroupHistoryFormat(val body: com.jsoniter.any.Any) {

    fun getMessage(): String {
        return "Отправлено уведомление: ${body[MailGroupHistoryFields.MESSAGE.value]}"
    }

    fun getTargetId(): UUID? {
        return when (val target = body[MailGroupHistoryFields.TARGET.value]) {
            null -> null
            else -> getOptUUID(CommonFields.ID.value, target)
        }
    }

    fun getTargetName(): String? {
        return when (val target = body[MailGroupHistoryFields.TARGET.value]) {
            null -> null
            else -> target.getOpt(CommonFields.NAME.value)?.asOpt()
        }
    }

    fun getTargetType(): String? {
        return when (val target = body[MailGroupHistoryFields.TARGET.value]) {
            null -> null
            else -> target.getOpt(CommonFields.TYPE.value)?.asOpt()
        }
    }

    fun getCreatedBy(): UUID? {
        return getOptUUID(MailGroupFields.CREATED_BY.value, body)
    }

}