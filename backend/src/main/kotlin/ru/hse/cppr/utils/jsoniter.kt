package ru.hse.cppr.utils

import com.jsoniter.ValueType
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import java.util.*

inline fun <reified A: kotlin.Any> Any.`as`(): A {
    return this.`as`(A::class.java)
}

inline fun <reified A: kotlin.Any> Any.asOpt(): A? {

    return when (valueType()) {
        ValueType.NULL -> null
        else           -> `as`()
    }
}

inline fun Any.getOpt(key: kotlin.Any): Any? {

    return when (keys().contains(key)) {
        true  -> when (get(key).valueType()) {
            ValueType.NULL -> null
            else -> get(key)
        }
        false -> null
    }
}

fun formJsonResponse(status: String, message: String): String {

    return JsonStream.serialize(
        Any.wrap(mapOf(
            "status" to status,
            "message" to message
        )))
}


fun getOptUUID(key: String, body: com.jsoniter.any.Any): UUID? {
    return when(val id = body.getOpt(key)?.asOpt<String>()) {
        null -> null
        else -> UUID.fromString(id)
    }
}

fun getOptUUID(id: kotlin.Any?): UUID? {
    return when(id) {
        null -> null
        else -> UUID.fromString(id.toString())
    }
}

fun getUUID(id: kotlin.Any): UUID {
    return UUID.fromString(id.toString())
}

fun getOptUUIDDefault(key: String, body: com.jsoniter.any.Any): UUID? {
    return when(val id = body.getOpt(key)?.asOpt<String>()) {
        null -> UUID.fromString("00000000-0000-0000-0000-000000000000")
        else -> UUID.fromString(id)
    }
}