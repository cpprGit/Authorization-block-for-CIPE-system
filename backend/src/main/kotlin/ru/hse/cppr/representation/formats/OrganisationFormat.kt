package ru.hse.cppr.representation.formats

import org.joda.time.DateTime
import ru.hse.cppr.data.database_generated.enums.OrganisationStatus
import ru.hse.cppr.data.database_generated.enums.OrganisationType
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.OrganisationFields
import ru.hse.cppr.utils.getOptUUID
import java.sql.Timestamp
import java.util.*


class OrganisationFormat(val body: com.jsoniter.any.Any) {

    fun getName(): String {
        return body[OrganisationFields.NAME.value].toString()
    }

    fun getIsHseDepartment(): Boolean {
        return body[OrganisationFields.IS_HSE_DEPARTMENT.value].toBoolean()
    }

    fun getParent(): UUID? {
        return getOptUUID(OrganisationFields.PARENT.value, body)
    }



    fun getSchemaCotnentId(): UUID {
        return UUID.fromString(body[CommonFields.SCHEMA_CONTENT_ID.value].toString())
    }

    fun getCreatedBy(): UUID {
        return UUID.fromString(body[CommonFields.CREATED_BY.value].toString())
    }

    fun getLastModifiedBy(): UUID {
        return UUID.fromString(body[OrganisationFields.LAST_MODIFIED_BY.value].toString())
    }

    fun getLastModifiedTime(): Timestamp {
        return Timestamp(DateTime.parse(body[OrganisationFields.LAST_MODIFIED_TIME.value].toString()).millis)
    }

    fun getType(): OrganisationType {
        return OrganisationType.valueOf(body[CommonFields.TYPE.value].toString())
    }

    fun getStatus(): OrganisationStatus {
        return OrganisationStatus.valueOf(body[OrganisationFields.STATUS.value].toString())
    }

}