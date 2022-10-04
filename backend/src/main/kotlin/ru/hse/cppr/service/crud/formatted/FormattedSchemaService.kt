package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import arrow.fx.typeclasses.Concurrent
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.SchemaAttributesDictionaryService
import java.util.*
import kotlin.collections.ArrayList
import org.jooq.impl.DSL.*
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.service.crud.AttributesDictionaryService
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.service.crud.SchemaService
import ru.hse.cppr.utils.*
import kotlin.streams.toList


open class FormattedSchemaService(override val serviceName: String): KoinComponent,
    CRUDService {
    val provider: TxProvider<ForIO>                                     by inject()
    val concurrent: Concurrent<ForIO>                                   by inject()
    val log: Log                                                        by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    val schemaService: SchemaService                                    by inject()
    val attributesService: AttributesDictionaryService                  by inject()

    val tableSchemas = Tables.SCHEMAS_DICTIONARY
    val tableAttributes = Tables.ATTRIBUTES
    val tableSchemaAttributes = Tables.SCHEMA_ATTRIBUTES

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val txResult = provider.tx { configuration ->
            val schemasIds = using(configuration)
                .select(tableSchemas.ID)
                .from(tableSchemas)
                .where(tableSchemas.SCHEMA_TYPE.notEqual(SchemaType.student_profile)
                    .and(tableSchemas.SCHEMA_TYPE.notEqual(SchemaType.user_profile))
                    .and(tableSchemas.SCHEMA_TYPE.notEqual(SchemaType.project))
                    .and(tableSchemas.ARCHIVED.eq(false)))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[tableSchemas.ID])

                    list
                }

            val schemas = ArrayList<Map<String, Any?>>()

            for (schemaId in schemasIds) {
                schemas.add(getFormattedSchemaProgram(configuration, schemaId))
            }

            schemas
        }


        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Something went wrong")
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    log.i("Program executed successfully")
                    return@runBlocking cb.b
                }
            }
        }
    }



    override fun get(id: UUID): Map<String, Any?> {
        val txResult = provider.tx { configuration ->
           getFormattedSchemaProgram(configuration, id)
        }


        val res = runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Something went wrong")
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    log.i("Program executed successfully")
                     return@runBlocking cb.b
                }
            }
        }

        return res
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val schemaId = UUID.fromString(schemaService.createSchemaProgram(configuration, body)["id"].toString())

            for (attr in body["attributes"].distinctBy { attr -> attr["id"].`as`<String>() }) {
                using(configuration)
                    .insertInto(tableSchemaAttributes)
                    .columns(SchemaAttributesDictionaryService.COLUMNS)
                    .values(
                        schemaId,
                        attr["id"].`as`<String>().let(UUID::fromString)
                    )
                    .returning(tableSchemaAttributes.ID)
                    .fetch()
            }

            schemaId
        }

        val mixedProgram = concurrent.bindingConcurrent {
            val resId = txResult.bind()
            listOf(resId)
        }


        val res = runBlocking {
            when (val cb = Either.catch { mixedProgram.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Something went wrong")
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    log.i("Program executed successfully")
                    return@runBlocking cb.b
                }
            }
        }


        return mapOf("id" to res[0].toString())
    }



    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
       return schemaService.update(id, body)
    }

    override fun delete(id: UUID): String {
        return schemaService.delete(id)
    }

    fun getFormattedSchemaProgram(configuration: Configuration, id: UUID): MutableMap<String, Any?> {
        val schemaM = schemaService.getSchemaProgram(id, configuration).toMutableMap()

        val attributesIds = using(configuration)
            .select(tableAttributes.ID)
            .from(tableAttributes)
            .innerJoin(tableSchemaAttributes)
            .on(tableAttributes.ID.eq(tableSchemaAttributes.ATTRIBUTE_ID))
            .where(tableSchemaAttributes.SCHEMA_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[tableAttributes.ID]
                )

                list
            }


        val fields = getFields(schemaM["schemaType"], configuration)

        val fieldsNames = fields.stream()
            .map { field -> field["attribute"] as Map<*, *> }
            .map { attr -> attr["name"].toString()}
            .toList()


        val attributes = ArrayList<Map<String, kotlin.Any?>>()

        for (attrId in attributesIds) {
            val attr = attributesService.getAttributeProgram(configuration, attrId)
            if (!fieldsNames.contains(attr["name"].toString())) {
                attributes.add(attr)
            }
        }


        schemaM["attributes"] = attributes
        schemaM["fields"] = fields

        return schemaM
    }


    fun getWithoutFieldsProgram(configuration: Configuration, id: UUID): Map<String, Any?> {
        val schemaM = schemaService.getSchemaProgram(id, configuration).toMutableMap()

        val attributesIds = using(configuration)
            .select(tableAttributes.ID)
            .from(tableAttributes)
            .innerJoin(tableSchemaAttributes)
            .on(tableAttributes.ID.eq(tableSchemaAttributes.ATTRIBUTE_ID))
            .where(tableSchemaAttributes.SCHEMA_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[tableAttributes.ID]
                )

                list
            }


        val fields = getFields(schemaM["schemaType"], configuration)

        val fieldsNames = fields.stream()
            .map { field -> field["attribute"] as Map<*, *> }
            .map { attr -> attr["name"].toString()}
            .toList()


        val attributes = ArrayList<Map<String, kotlin.Any?>>()

        for (attrId in attributesIds) {
            val attr = attributesService.getAttributeProgram(configuration, attrId)
            if (!fieldsNames.contains(attr["name"].toString())) {
                attributes.add(attr)
            }
        }


        schemaM["attributes"] = attributes

        return schemaM
    }

    private fun getFields(schemaType: Any?, configuration: Configuration): MutableList<Map<String, Any>> {
        return when(schemaType) {
            null -> mutableListOf()
            else -> getFields(configuration, SchemaType.valueOf(schemaType.toString()))
        }
    }
}