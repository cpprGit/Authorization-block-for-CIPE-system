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
import ru.hse.cppr.data.database_generated.tables.records.ProjectsRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.*
import java.util.*

class ProjectService(override val serviceName: String) : KoinComponent, CRUDService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    val table = Tables.PROJECTS
    val activityTable = Tables.ACTIVITY

    val COLUMNS = arrayListOf(
        table.CONSULTANT_ID,
        table.LEADER_ID,
        table.ACTIVITY_ID,
        table.SCHEMA_CONTENT_ID,

        table.NAME_RUS,
        table.NAME_ENG,
        table.IS_GROUP_PROJECT,
        table.TYPE,
        table.MAX_STUDENTS,

        table.PI,
        table.PMI,
        table.PAD
    )
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            ProjectFields.ID.value to record[table.ID]?.toString(),
            ProjectFields.CONSULTANT_ID.value to record[table.CONSULTANT_ID]?.toString(),
            ProjectFields.MENTOR_ID.value to record[table.LEADER_ID]?.toString(),
            ProjectFields.ACTIVITY_ID.value to record[table.ACTIVITY_ID]?.toString(),
            CommonFields.SCHEMA_CONTENT_ID.value to record[table.SCHEMA_CONTENT_ID]?.toString(),

            ProjectFields.PROJECT_NAME_RUS.value to record[table.NAME_RUS]?.toString(),
            ProjectFields.PROJECT_NAME_ENG.value to record[table.NAME_ENG]?.toString(),
            ProjectFields.PROJECT_INDIVIDUALITY.value to convertProjectIndividuality(record[table.IS_GROUP_PROJECT]),
            ProjectFields.PROJECT_TYPE.value to convertProjectType(record[table.TYPE]),
            ProjectFields.MAX_STUDENTS.value to record[table.MAX_STUDENTS],

            ProjectFields.PI_COURSES.value to record[table.PI]?.toString(),
            ProjectFields.PMI_COURSES.value to record[table.PMI]?.toString(),
            ProjectFields.PAD_COURSES.value to record[table.PAD]?.toString()
        )
    }


    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val projectM = provider.tx { configuration ->
            createProjectProgram(body, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { projectM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val projectsM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
                .from(table)
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(fetchMapValues(record))

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { projectsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun get(id: UUID): Map<String, Any?> {
        val projectM = provider.tx { configuration ->
            getProjectProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { projectM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    override fun update(id: UUID, body: com.jsoniter.any.Any): String = with(table) {
        val projectM = provider.tx { configuration ->
            updateProgram(id, body, configuration)
        }

        val project = runBlocking {
            when (val cb = Either.catch { projectM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (project.isPresent) {
            false -> throw BadRequestException("Project with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(project.get()))
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

    fun getProjectProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .select(COLUMNS_WITH_ID)
            .from(table)
            .where(table.ID.eq(id))
            .fetchOne()
            .map {
                fetchMapValues(it)
            }
    }

    fun createProjectProgram(body: com.jsoniter.any.Any, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                getOptUUID(ProjectFields.CONSULTANT_ID.value, body),
                body[ProjectFields.MENTOR_ID.value].`as`<String>().let(UUID::fromString),
                getOptUUID(ProjectFields.ACTIVITY_ID.value, body),
                getOptUUID(CommonFields.SCHEMA_CONTENT_ID.value, body),

                body[ProjectFields.PROJECT_NAME_RUS.value].`as`(),
                body.getOpt(ProjectFields.PROJECT_NAME_ENG.value)?.asOpt(),
                body.getOpt(ProjectFields.PROJECT_INDIVIDUALITY.value)?.asOpt(),
                body.getOpt(ProjectFields.PROJECT_TYPE.value)?.asOpt(),
                body.getOpt(ProjectFields.MAX_STUDENTS.value)?.asOpt(),

                body.getOpt(ProjectFields.PI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PMI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PAD_COURSES.value)?.asOpt()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }

    private fun getActivityIdByName(name: String, configuration: Configuration): UUID {
        return when (name) {
            "" -> DSL.using(configuration)
                .selectFrom(activityTable)
                .where(activityTable.ID.eq(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                .fetchOne()
                .map { it[activityTable.ID] }
            else -> DSL.using(configuration)
                .selectFrom(activityTable)
                .where(activityTable.NAME.eq(name))
                .fetchOne()
                .map { it[activityTable.ID] }
        }

    }

    fun updateProgram(id: UUID, body: com.jsoniter.any.Any, configuration: Configuration): Optional<Map<String, Any?>> = with(table) {
        val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<ProjectsRecord>>(
            DSL.using(configuration).update(table)
        ) { statement, key ->
            when (val key = key.toString()) {
                ProjectFields.CONSULTANT.value ->
                    statement.set(CONSULTANT_ID, getOptUUID(CommonFields.ID.value, body[key]))
                ProjectFields.MENTOR.value ->
                    statement.set(LEADER_ID, UUID.fromString(body[key][CommonFields.ID.value].toString()))
                ProjectFields.ACTIVITY.value ->
                    statement.set(ACTIVITY_ID, getActivityIdByName(body[key].toString(), configuration))
                CommonFields.SCHEMA_CONTENT_ID.value ->
                    statement.set(SCHEMA_CONTENT_ID, UUID.fromString(body[key].toString()))
                ProjectFields.PROJECT_NAME_RUS.value ->
                    statement.set(NAME_RUS, body[key].`as`<String>())
                ProjectFields.PROJECT_NAME_ENG.value ->
                    statement.set(NAME_ENG, body[key].`as`<String>())
                ProjectFields.PROJECT_INDIVIDUALITY.value ->
                    statement.set(IS_GROUP_PROJECT, convertProjectIndividuality(body[key].toString()))
                ProjectFields.PROJECT_TYPE.value ->
                    statement.set(TYPE, convertProjectType(body[key].toString()))
                ProjectFields.MAX_STUDENTS.value ->
                    statement.set(MAX_STUDENTS, body[key].asOpt<Int>())
                ProjectFields.PI_COURSES.value ->
                    statement.set(PI, body[key].asOpt<String>())
                ProjectFields.PMI_COURSES.value ->
                    statement.set(PMI, body[key].asOpt<String>())
                ProjectFields.PAD_COURSES.value ->
                    statement.set(PAD, body[key].asOpt<String>())
                else ->
                    statement
            }
        }

        return when (updateStatement) {
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

}