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
import ru.hse.cppr.data.database_generated.enums.ProjectRequestStatus
import ru.hse.cppr.data.database_generated.tables.records.ProjectRequestsRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.utils.*
import java.util.*
import kotlin.collections.ArrayList

class ProjectRequestService(override val serviceName: String) : KoinComponent, CRUDService {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    val table = Tables.PROJECT_REQUESTS

    val COLUMNS = arrayListOf(
        table.CONSULTANT_ID,
        table.LEADER_ID,
        table.SCHEMA_CONTENT_ID,

        table.NAME_RUS,
        table.NAME_ENG,
        table.IS_GROUP_PROJECT,
        table.STATUS,
        table.TYPE,

        table.PI,
        table.PMI,
        table.PAD,

        table.CREATED_BY
    )
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            ProjectFields.ID.value to record[table.ID]?.toString(),
            ProjectFields.CONSULTANT_ID.value to record[table.CONSULTANT_ID]?.toString(),
            ProjectFields.MENTOR_ID.value to record[table.LEADER_ID]?.toString(),
            CommonFields.SCHEMA_CONTENT_ID.value to record[table.SCHEMA_CONTENT_ID]?.toString(),

            ProjectFields.PROJECT_NAME_RUS.value to record[table.NAME_RUS]?.toString(),
            ProjectFields.PROJECT_NAME_ENG.value to record[table.NAME_ENG]?.toString(),
            ProjectFields.PROJECT_INDIVIDUALITY.value to convertProjectIndividuality(record[table.IS_GROUP_PROJECT]),
            ProjectFields.STATUS.value to convertProjectRequestStatus(record[table.STATUS]),
            ProjectFields.PROJECT_TYPE.value to convertProjectType(record[table.TYPE]),


            ProjectFields.PI_COURSES.value to record[table.PI]?.toString(),
            ProjectFields.PMI_COURSES.value to record[table.PMI]?.toString(),
            ProjectFields.PAD_COURSES.value to record[table.PAD]?.toString(),

            ProjectFields.CREATED_BY.value to record[table.CREATED_BY]?.toString(),
            ProjectFields.CREATE_DATE.value to record[table.CREATE_DATE]?.toString()
        )
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val projectRequestM = provider.tx { configuration ->
            createProjectRequestProgram(
                body,
                body[CommonFields.SCHEMA_CONTENT_ID.value].toString(),
                configuration
            )
        }

        return runBlocking {
            when (val cb = Either.catch { projectRequestM.fix().unsafeRunSync() }) {
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
        val projectRequestM = provider.tx { configuration ->
            getProjectRequestProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { projectRequestM.fix().unsafeRunSync() }) {
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

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<ProjectRequestsRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    ProjectFields.CONSULTANT.value ->
                        statement.set(CONSULTANT_ID, getOptUUID(CommonFields.ID.value, body[key]))
                    ProjectFields.MENTOR.value ->
                        statement.set(LEADER_ID, UUID.fromString(body[key][CommonFields.ID.value].toString()))
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

        val projectRequest = runBlocking {
            when (val cb = Either.catch { projectM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (projectRequest.isPresent) {
            false -> throw BadRequestException("Project request with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(projectRequest.get()))
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

    fun getProjectRequestProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .select()
            .from(table)
            .where(table.ID.eq(id))
            .fetchOne()
            .map {
                fetchMapValues(it)
            }
    }

    fun createProjectRequestProgram(body: com.jsoniter.any.Any, schemaContentId: String, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                getOptUUID(CommonFields.ID.value, body[ProjectFields.CONSULTANT.value]),
                body[ProjectFields.MENTOR.value][CommonFields.ID.value].toString().let(UUID::fromString),
                UUID.fromString(schemaContentId),

                body[ProjectFields.PROJECT_NAME_RUS.value].`as`(),
                body.getOpt(ProjectFields.PROJECT_NAME_ENG.value)?.asOpt(),
                convertProjectIndividuality(body.getOpt(ProjectFields.PROJECT_INDIVIDUALITY.value)?.asOpt()),
                ProjectRequestStatus.pending,
                convertProjectType(body.getOpt(ProjectFields.PROJECT_TYPE.value)?.asOpt()).toString(),

                body.getOpt(ProjectFields.PI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PMI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PAD_COURSES.value)?.asOpt(),

                body[CommonFields.CREATED_BY.value].toString().let(UUID::fromString)
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }


}