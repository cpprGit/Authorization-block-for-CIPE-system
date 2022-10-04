package ru.hse.cppr.routing

import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.SchemaAttributesDictionaryService.deleteSchemaAttributeRecord
import ru.hse.cppr.service.SchemaAttributesDictionaryService.persistSchemaAttributeRecord
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formCreatedResponse
import ru.hse.cppr.utils.formOkResponse
import java.util.*

object SchemaAttributeDispatcher: KoinComponent {
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun addAttributeToSchemaPost(exchange: HttpServerExchange): Unit {
        val body = runBlocking { JsonIterator.deserialize(exchange.body()) }
        formCreatedResponse(exchange, JsonStream.serialize(com.jsoniter.any.Any.wrap(persistSchemaAttributeRecord(body))))
    }

    fun removeAttribureFromSchemaDelete(exchange: HttpServerExchange): Unit {
        val attr_id = exchange.queryParameters["attr_id"]!!.first().let(UUID::fromString)
        val schema_id = exchange.queryParameters[CommonFields.SCHEMA_ID.value]!!.first().let(UUID::fromString)

        formOkResponse(exchange, deleteSchemaAttributeRecord(attr_id, schema_id))
    }
}