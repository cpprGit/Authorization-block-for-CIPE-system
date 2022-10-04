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
import ru.hse.cppr.data.database_generated.tables.records.SchemaContentRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.`as`
import java.util.*

class SchemaContentService(override val serviceName: String) : KoinComponent, CRUDService {

    private val provider: TxProvider<ForIO> by inject()
    private val log: Log by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.SCHEMA_CONTENT

    val COLUMNS = arrayListOf(table.SCHEMA_ID, table.CONTENT)
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            table.ID.name to record[table.ID].toString(),
            table.SCHEMA_ID.name to record[table.SCHEMA_ID].toString(),
            table.CONTENT.name to record[table.CONTENT].toString()
        )
    }


    override fun create(body: Any): Map<String, kotlin.Any?> = with (table) {
        val formM = provider.tx { configuration ->
           createProgram(body, configuration)
        }

        val form = runBlocking {
            when (val cb = Either.catch { formM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to create form.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return form
    }

    override fun list(vararg params: String): MutableList<Map<String, kotlin.Any?>> = with (table) {
        val formListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
                .from(table)
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(fetchMapValues(record))
                    list
                }
        }

        val formList = runBlocking {
            when (val cb = Either.catch { formListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain form list.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return formList
    }


    override fun get(id: UUID): Map<String, kotlin.Any?> = with (table) {
        val formListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
                .from(table)
                .where(ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(fetchMapValues(record))

                    list
                }
        }


        val form = runBlocking {
            when (val cb = Either.catch { formListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain form.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Form found.")
                    return@runBlocking cb.b
                }
            }
        }

        val res = form.firstOrNull()
        when(res) {
            null -> throw BadRequestException("Bad Id.")
            else -> return res
        }
    }


    override fun update(id: UUID, body: Any): String = with(table) {
        val formM = provider.tx { configuration ->
            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<SchemaContentRecord>>(DSL.using(configuration).update(
                table
            )) { statement, key ->
                when (val key = key.toString()) {
                    CommonFields.SCHEMA_ID.value ->
                        statement.set(SCHEMA_ID, body[key].`as`<String>().let(UUID::fromString))
                    CommonFields.SCHEMA_CONTENT.value ->
                        statement.set(CONTENT, body[key].`as`<String>())

                    else ->
                        statement
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

        val form = runBlocking {
            when (val cb = Either.catch { formM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch Form.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (form.isPresent) {
            false -> throw BadRequestException("Form with id=$id not found.")
            true -> return JsonStream.serialize(Any.wrap(form))
        }
    }

    override fun delete(id: UUID): String = with(table) {
        val deleteM = provider.tx { configuration ->
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
                    throw BadRequestException("Bad id.")
                }
            }) {
                is Either.Left -> {
                    log.e("Unable to delete form.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return "{ \"status\": \"Success\"}"
    }

    fun createProgram(body: Any, configuration: Configuration) : Map<String, String> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body[CommonFields.SCHEMA_ID.value].`as`<String>().let(UUID::fromString),
                body[CommonFields.SCHEMA_CONTENT.value].`as`<String>()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let { fetchMapValues(it) }
    }

}