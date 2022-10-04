package ru.hse.cppr.representation.formats

import com.jsoniter.any.Any
import ru.hse.cppr.representation.enums.fields.AttributeFields
import ru.hse.cppr.utils.asOpt
import ru.hse.cppr.utils.getOpt


class AttributeFormat(val body: com.jsoniter.any.Any) {

    fun getUsage(): String {
        return body[AttributeFields.USAGE.value].toString()
    }

    fun getName(): String {
        return body[AttributeFields.NAME.value].toString()
    }

    fun getTitle(): String {
        return body[AttributeFields.TITLE.value].toString()
    }

    fun getDescription(): String? {
        return body.getOpt(AttributeFields.DESCRIPTION.value)?.asOpt()
    }

    fun getPlaceholder(): String? {
        return body.getOpt(AttributeFields.PLACEHOLDER.value)?.asOpt()
    }

    fun getHint(): String? {
        return body.getOpt(AttributeFields.HINT.value)?.asOpt()
    }

    fun getStep(): Int? {
        return body.getOpt(AttributeFields.STEP.value)?.asOpt()
    }

    fun getMin(): Int? {
        return body.getOpt(AttributeFields.MIN.value)?.asOpt()
    }

    fun getMax(): Int? {
        return body.getOpt(AttributeFields.MAX.value)?.asOpt()
    }

    fun getMandatory(): Boolean? {
        return body.getOpt(AttributeFields.MANDATORY.value)?.asOpt() ?: false
    }

    fun getValueDefault(): String? {
        return body.getOpt(AttributeFields.VALUE_DEFAULT.value)?.asOpt()
    }

    fun getVariants(): Any {
        return body[AttributeFields.VARIANTS.value]
    }

    fun getValidators(): Any {
        return body[AttributeFields.VALIDATORS.value]
    }

    fun getHasOtherVariant(): Boolean? {
        return body.getOpt(AttributeFields.HAS_OTHER_VARIANT.value)?.asOpt() ?: false
    }
}