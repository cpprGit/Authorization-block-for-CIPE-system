package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.standalone.inject
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.service.crud.SchemaContentService
import java.util.*

class FormattedSchemaContentService(serviceName: String) : FormattedSchemaService(serviceName) {

    val schemaContentService: SchemaContentService                      by inject()
    val tableSchemasContent = Tables.SCHEMA_CONTENT

    var COLUMNS_WITH_CONTENT = arrayListOf(
        tableSchemas.ID,
        tableSchemas.NAME,
        tableSchemas.DESCRIPTION,
        tableSchemas.SCHEMA_TYPE,
        tableSchemas.CREATED_BY,
        tableSchemas.BUTTON_NAME,
        tableSchemasContent.CONTENT
    )


    override fun get(id: UUID): Map<String, Any?> {
        val txResult = provider.tx { configuration ->
            getFormattedSchemaContentProgram(configuration, id)
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


    override fun create(body: com.jsoniter.any.Any): Map<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(id: UUID): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun getFormattedSchemaContentProgram(configuration: Configuration, id: UUID): Map<String, Any?> {
        val schemaContent = DSL.using(configuration)
            .select(tableSchemasContent.SCHEMA_ID, tableSchemasContent.CONTENT)
            .from(tableSchemasContent)
            .where(tableSchemasContent.ID.eq(id))
            .fetchOne()
            .map { record ->
                mapOf(
                    CommonFields.SCHEMA_ID.value to record[tableSchemasContent.SCHEMA_ID],
                    CommonFields.SCHEMA_CONTENT.value to record[tableSchemasContent.CONTENT]
                )
            }

        val schema = getFormattedSchemaProgram(configuration, UUID.fromString(schemaContent[CommonFields.SCHEMA_ID.value].toString()))
        schema[CommonFields.SCHEMA_CONTENT.value] = schemaContent[CommonFields.SCHEMA_CONTENT.value]
        schema[CommonFields.SCHEMA_CONTENT_ID.value] = id.toString()
        return schema
    }

}