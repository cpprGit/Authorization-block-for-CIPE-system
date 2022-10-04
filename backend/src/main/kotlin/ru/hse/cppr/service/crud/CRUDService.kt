package ru.hse.cppr.service.crud

import com.jsoniter.any.Any
import ru.hse.cppr.service.Service
import java.util.*

interface CRUDService: Service {

    fun create(body: Any): Map<String, kotlin.Any?>

    fun list(vararg params: String): MutableList<Map<String, kotlin.Any?>>

    fun get(id: UUID): Map<String, kotlin.Any?>

    fun update(id: UUID, body: Any): String

    fun delete(id: UUID): String
}