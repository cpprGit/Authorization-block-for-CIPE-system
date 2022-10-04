package ru.hse.cppr.service

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import kotlinx.coroutines.runBlocking
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import java.util.*

object SchemaAttributesDictionaryService : KoinComponent {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.SCHEMA_ATTRIBUTES

    val COLUMNS = arrayListOf(table.SCHEMA_ID, table.ATTRIBUTE_ID)
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            table.ID.name to record[table.ID].toString(),
            table.SCHEMA_ID.name to record[table.SCHEMA_ID].toString(),
            table.ATTRIBUTE_ID.name to record[table.ATTRIBUTE_ID].toString()
        )
    }



    fun persistSchemaAttributeRecord(body: Any) : Map<String, String> = with(table) {
        val attrsM = provider.tx { configuration ->
            DSL.using(configuration)
                .insertInto(table)
                .columns(COLUMNS)
                .values(
                    UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()),
                    UUID.fromString(body["attribute_id"].toString())
                )
                .returning(COLUMNS_WITH_ID)
                .fetchOne()
                .let {
                    fetchMapValues(it)
                }
        }

        val persistedRecord = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to create new schema-attribute record.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schema-attribute record creation success.")
                    return@runBlocking cb.b
                }
            }
        }

        return persistedRecord
    }

    fun deleteSchemaAttributeRecord(attr_id: UUID, schema_id: UUID) : String = with(table) {
        val attrsM = provider.tx { configuration ->
            DSL.using(configuration)
                .deleteFrom(table)
                .where(SCHEMA_ID.eq(schema_id).and(ATTRIBUTE_ID.eq(attr_id)))
                .returning(COLUMNS_WITH_ID)
                .execute()
        }

        val deletedRecord = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to delete schema-attribute record.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schema-attribute record deletion success.")
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(Any.wrap(deletedRecord))
    }


    fun deleteSchemaAttributeRecord(id: UUID) : String = with(table) {
        val attrsM = provider.tx { configuration ->
            DSL.using(configuration)
                .deleteFrom(table)
                .where(ID.eq(id))
                .returning(COLUMNS_WITH_ID)
                .execute()
        }

        val deletedRecord = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to delete schema-attribute record.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Schema-attribute record deletion success.")
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(Any.wrap(deletedRecord))
    }


    fun getAttributesBySchema(schema_id: UUID) : MutableList<Map<String, kotlin.Any>> = with(table) {
        val attrsM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS)
                .from(table)
                .where(SCHEMA_ID.eq(schema_id))
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any>>()) { list, record ->
                    list.add(mapOf(
                        "attribute_id" to record[ATTRIBUTE_ID].toString(),
                        CommonFields.SCHEMA_ID.value to record[SCHEMA_ID].toString()
                    ))
                    list
                }
        }

        val attributes = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to get attributes by schema-id=$schema_id.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return attributes
    }

}