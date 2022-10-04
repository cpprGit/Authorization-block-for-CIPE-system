package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import arrow.mtl.extensions.statet.monadState.state
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.*
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.tables.records.TaskRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.`as`
import ru.hse.cppr.utils.asOpt
import ru.hse.cppr.utils.getOpt
import java.sql.Timestamp
import java.util.*

class TasksService(override val serviceName: String) : KoinComponent,
    CRUDService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.TASK


    val COLUMNS = arrayListOf(
        table.NAME,
        table.DESCRIPTION,
        table.IS_UPLOADABLE
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            table.ID.name to record[table.ID]?.toString(),
            table.NAME.name to record[table.NAME]?.toString(),
            table.DESCRIPTION.name to record[table.DESCRIPTION]?.toString(),
            "isUploadable" to record[table.IS_UPLOADABLE]
        )
    }


    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val taskM = provider.tx { configuration ->
            createTaskProgram(body, configuration)
        }

        val persistedTask = runBlocking {
            when (val cb = Either.catch { taskM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return persistedTask
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val taskListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
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

        val foundTaskList = runBlocking {
            when (val cb = Either.catch { taskListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return foundTaskList
    }

    override fun get(id: UUID): Map<String, Any?> = with(table) {
        val taskGet = provider.tx { configuration ->
            getTaskProgram(id, configuration)
        }

        val taskList = runBlocking {
            when (val cb = Either.catch { taskGet.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        val res = taskList.firstOrNull()
        when (res) {
            null -> throw BadRequestException("Bad Id.")
            else -> return res
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String = with(table) {
        val taskM = provider.tx { configuration ->

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<TaskRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    "name" ->
                        statement.set(NAME, body[key].`as`<String>())
                    "description" ->
                        statement.set(DESCRIPTION, body[key].`as`<String>())
                    "is_uploadable" ->
                        statement.set(IS_UPLOADABLE, body[key].`as`<Boolean>())
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

        val task = runBlocking {
            when (val cb = Either.catch { taskM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (task.isPresent) {
            false -> throw BadRequestException("Task with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(task.get()))
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
                    throw BadRequestException("Bad Id.")
                }
            }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return "{ \"status\": \"Success\"}"
    }


    fun createTaskProgram(body: com.jsoniter.any.Any, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body["name"].`as`<String>(),
                body.getOpt("description")?.asOpt(),
                body.getOpt("isUploadable")?.asOpt()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }

    fun getTaskProgram(id: UUID, configuration: Configuration): MutableList<Map<String, Any?>> = with(table){
        return DSL.using(configuration)
            .select(COLUMNS_WITH_ID)
            .from(table)
            .where(ID.eq(id))
            .orderBy(NAME.asc())
            .fetch()
            .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                list.add(
                    fetchMapValues(record)
                )

                list
            }
    }
}