package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.jooq.*
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.data.database_generated.tables.records.SchemasDictionaryRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.utils.*
import java.util.*

class SchemaService(override val serviceName: String) : KoinComponent, CRUDService {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.SCHEMAS_DICTIONARY
    private val schemaAttrsTable = Tables.SCHEMA_ATTRIBUTES
    private val currentSchemaTable = Tables.CURRENT_SCHEMAS

    val COLUMNS = arrayListOf(table.NAME, table.DESCRIPTION, table.SCHEMA_TYPE, table.CREATED_BY, table.BUTTON_NAME)
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            "id" to record[table.ID].toString(),
            "title" to record[table.NAME],
            "description" to record[table.DESCRIPTION],
            "schemaType" to record[table.SCHEMA_TYPE],
            "createdBy" to record[table.CREATED_BY]?.toString(),
            "buttonName" to record[table.BUTTON_NAME],
            "archived" to record[table.ARCHIVED]
        )
    }


    override fun create(body: Any): Map<String, kotlin.Any?> {
        val schemaM = provider.tx { configuration ->
            createSchemaProgram(configuration, body)
        }

        val persistedSchema = runBlocking {
            when (val cb = Either.catch { schemaM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to create new schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schema creation success. \'$body\'")
                    return@runBlocking cb.b
                }
            }
        }

        return persistedSchema
    }

    override fun list(vararg params: String): MutableList<Map<String, kotlin.Any?>> {
        val schemaListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(table)
                .orderBy(table.NAME.asc())
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(
                        fetchMapValues(record)
                    )

                    list
                }
        }

        val foundSchemaList = runBlocking {
            when (val cb = Either.catch { schemaListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain schema list.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schemas found.")
                    return@runBlocking cb.b
                }
            }
        }

        return foundSchemaList
    }

    override fun get(id: UUID): Map<String, kotlin.Any?> = with(table) {
        val schemaListM = provider.tx { configuration ->
            getSchemaProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { schemaListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schema found.")
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun update(id: UUID, body: Any): String = with(table) {
        val schemaM = provider.tx { configuration ->

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<SchemasDictionaryRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    "title" ->
                        statement.set(NAME, body[key].`as`<String>())
                    "description" ->
                        statement.set(DESCRIPTION, stringOrEmpty(body[key]?.toString()))
                    "type" ->
                        statement.set(SCHEMA_TYPE, SchemaType.valueOf(body[key].toString()))
                    "buttonName" ->
                        statement.set(BUTTON_NAME, body[key].`as`<String>())
                    "attributes" -> {
                        updateSchemaAttributesProgram(id, body, configuration)
                        statement.set(ID, ID)
                    }
                    else ->
                        statement
                }
            }

            val currentSchema = DSL.using(configuration)
                .select(currentSchemaTable.SCHEMA_ID, currentSchemaTable.TYPE)
                .from(currentSchemaTable)
                .where(currentSchemaTable.SCHEMA_ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any>>()) { list, record ->
                    list.add(mapOf(
                        "id" to record[currentSchemaTable.SCHEMA_ID],
                        "type" to record[currentSchemaTable.TYPE]
                    ))
                    list
                }

            if (currentSchema.isNotEmpty()) {
                val schema = currentSchema[0]
                val schemaType = SchemaType.valueOf(schema["type"].toString())
                val newBody = Any.wrap(mapOf(CommonFields.SCHEMA_ID.value to schema["id"].toString()))

                when (schemaType) {
                    SchemaType.student_registration -> CurrentSchemaService.updateCurrentSchemaForStudentProfileProgram(
                        schemaType,
                        newBody,
                        configuration
                    )
                    SchemaType.user_registration -> CurrentSchemaService.updateCurrentSchemaForUserProfileProgram(
                        schemaType,
                        newBody,
                        configuration
                    )
                    SchemaType.student_profile_template -> CurrentSchemaService.updateCurrentSchemaForStudentProfileProgram(
                        schemaType,
                        newBody,
                        configuration
                    )
                    else -> CurrentSchemaService.updateCurrentSchemaProgram(
                        schemaType,
                        UUID.fromString(newBody[CommonFields.SCHEMA_ID.value].toString()),
                        configuration
                    )
                }

            }

            when (updateStatement) {
                is UpdateSetMoreStep ->
                    updateStatement
                        .where(ID.eq(id))
                        .returning(COLUMNS_WITH_ID)
                        .fetchOptional()
                        .map { record ->
                            fetchMapValues(record)
                        }

                is UpdateSetFirstStep ->
                    Optional.empty()

                else ->
                    Optional.empty()
            }
        }

        val schema = runBlocking {
            when (val cb = Either.catch { schemaM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (schema.isPresent) {
            false -> throw BadRequestException("Schema with id=$id not found.")
            true -> return JsonStream.serialize(Any.wrap(schema.get()))
        }
    }

    override fun delete(id: UUID): String = with(table) {
        val deleteM = provider.tx { configuration ->

            val deleteAttrsFromSchema =
                DSL.using(configuration)
                    .deleteFrom(schemaAttrsTable)
                    .where(schemaAttrsTable.SCHEMA_ID.eq(id))
                    .execute()

            DSL.using(configuration)
                .delete(table)
                .where(ID.eq(id))
                .returning(COLUMNS_WITH_ID)
                .execute()
        }

        runBlocking {
            when (val cb = Either.catch {
                val deleted = deleteM.fix().unsafeRunSync()
                if (deleted == 0) {
                    throw BadRequestException("Bad Id.")
                }
            }) {
                is Either.Left -> {
                    log.e("Unable to delete schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.e("Schema deletion success.")
                    return@runBlocking cb.b
                }
            }
        }

        return "{ \"status\": \"Success\"}"
    }

    fun createSchemaProgram(configuration: Configuration, body: Any): Map<String, kotlin.Any?>  {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body["title"].`as`(),
                body.getOpt("description")?.asOpt(),
                body.getOpt("type")?.asOpt(),
                getOptUUID("createdBy", body),
                body["buttonName"].`as`<String>()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }


    fun updateSchemaAttributesProgram(id: UUID, body: Any, configuration: Configuration) {
        val deleteAttrsFromSchema =
            DSL.using(configuration)
                .deleteFrom(schemaAttrsTable)
                .where(schemaAttrsTable.SCHEMA_ID.eq(id))
                .execute()

        for (attr in body["attributes"]) {
            DSL.using(configuration)
                .insertInto(schemaAttrsTable)
                .columns(schemaAttrsTable.SCHEMA_ID, schemaAttrsTable.ATTRIBUTE_ID)
                .values(id, UUID.fromString(attr["id"].toString()))
                .execute()
        }
    }

    fun getSchemaProgram(id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        val res = DSL.using(configuration)
            .select()
            .from(table)
            .where(table.ID.eq(id))
            .orderBy(table.NAME.asc())
            .fetch()
            .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                list.add(
                    fetchMapValues(record)
                )

                list
            }

        //TODO: replace with NotFoundException
        return when(res.firstOrNull()) {
            null -> throw BadRequestException("Not Found")
            else -> res.first()
        }
    }

}