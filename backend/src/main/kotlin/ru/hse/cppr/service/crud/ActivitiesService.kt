package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
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
import ru.hse.cppr.data.database_generated.enums.ActivityStatus
import ru.hse.cppr.data.database_generated.enums.FacultyType
import ru.hse.cppr.data.database_generated.tables.records.ActivityRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.*
import java.util.*

class ActivitiesService(override val serviceName: String) : KoinComponent,
    CRUDService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.ACTIVITY

    val COLUMNS = arrayListOf(
        table.NAME,
        table.DESCRIPTION,
        table.FACULTY,
        table.YEAR,
        table.COURSE,
        table.STATUS,
        table.SCHEMA_CONTENT_ID
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            table.ID.name to record[table.ID]?.toString(),
            table.NAME.name to record[table.NAME]?.toString(),
            table.DESCRIPTION.name to record[table.DESCRIPTION]?.toString(),
            table.FACULTY.name to convertFaculty(record[table.FACULTY]),
            table.YEAR.name to record[table.YEAR]?.toString(),
            table.COURSE.name to record[table.COURSE]?.toString(),
            table.STATUS.name to convertActivityStatus(record[table.STATUS]),
            "schemaContentId" to record[table.SCHEMA_CONTENT_ID]?.toString()
        )
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val activityM = provider.tx { configuration ->
            createActivityProgram(body, body["schemaContentId"].toString(), configuration)
        }

        val persistedActivity = runBlocking {
            when (val cb = Either.catch { activityM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return persistedActivity
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val activityListM = provider.tx { configuration ->
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

        val foundActivityList = runBlocking {
            when (val cb = Either.catch { activityListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return foundActivityList
    }

    override fun get(id: UUID): Map<String, Any?> = with(table) {
        val activityM = provider.tx { configuration ->
            getActivityProgram(id, configuration)
        }

        val activitiesList = runBlocking {
            when (val cb = Either.catch { activityM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        val res = activitiesList.firstOrNull()
        when (res) {
            null -> throw BadRequestException("Bad Id.")
            else -> return res
        }
    }


    override fun update(id: UUID, body: com.jsoniter.any.Any): String = with(table) {
        val activityM = provider.tx { configuration ->

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<ActivityRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    "name" ->
                        statement.set(NAME, body[key].`as`<String>())
                    "description" ->
                        statement.set(DESCRIPTION, body[key].`as`<String>())
                    "faculty" ->
                        statement.set(FACULTY, FacultyType.valueOf(body[key].toString()))
                    "status" ->
                        statement.set(STATUS, ActivityStatus.valueOf(body[key].toString()))
                    "year" ->
                        statement.set(YEAR, body[key].`as`<Int>())
                    "course" ->
                        statement.set(COURSE, body["course"].`as`<Int>())
                    "schema_content_id" ->
                        statement.set(SCHEMA_CONTENT_ID, body["schema_content_id"].`as`<String>().let(UUID::fromString))
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

        val activity = runBlocking {
            when (val cb = Either.catch { activityM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (activity.isPresent) {
            false -> throw BadRequestException("Task with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(activity.get()))
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


    fun createActivityProgram(body: com.jsoniter.any.Any, schemaContentId: String, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body["name"].`as`<String>(),
                body.getOpt("description")?.asOpt(),
                convertFaculty(body["faculty"].`as`<String>()).name,
                body["year"].`as`<Int>(),
                body["course"].`as`<Int>(),
                convertActivityStatus(body.getOpt("status")?.asOpt<String>()),
                schemaContentId.let(UUID::fromString)
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }


    fun getActivityProgram(id: UUID, configuration: Configuration): MutableList<Map<String, Any?>> = with(table){
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