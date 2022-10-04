package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import org.jooq.UpdateSetStep
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.ActivityStatus
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.data.database_generated.tables.records.ActivityRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.ActivityFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.*
import ru.hse.cppr.utils.*
import java.sql.Timestamp
import java.util.*

class FormattedActivitiesService(override val serviceName: String): KoinComponent, CRUDService {

    val provider: TxProvider<ForIO>                                                         by inject()
    val log: Log                                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val schemaContentService: SchemaContentService                                  by inject()
    private val activitiesService: ActivitiesService                                        by inject()
    private val tasksService: TasksService                                                  by inject()
    private val stagesService: StagesService                                                by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService                by inject()

    private val tasksStageTable = Tables.TASK_STAGE
    private val activityStageTable = Tables.ACTIVITY_STAGE
    private val activityTable = Tables.ACTIVITY
    private val tasksTable = Tables.TASK
    private val stagesTable = Tables.STAGE


    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val activityM = provider.tx { configuration ->

            val createdActivityProfile = schemaContentService.createProgram(
                com.jsoniter.any.Any.wrap(mapOf(
                    CommonFields.SCHEMA_ID.value to CurrentSchemaService
                        .getCurrentSchemaProgram(SchemaType.activity, configuration)
                        .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                    CommonFields.SCHEMA_CONTENT.value to body[CommonFields.SCHEMA_CONTENT.value].toString()
                )),
                configuration
            )

            val newActivity = activitiesService.createActivityProgram(body,
                createdActivityProfile[ActivityFields.ID.value].toString(), configuration) as MutableMap

            val stages = body[ActivityFields.STAGES.value]

            for(stage in stages) {
                val newStage = stagesService.createStageProgram(stage, configuration)

                val tasks = stage[ActivityFields.TASKS.value]

                for(task in tasks) {
                    val newTask = tasksService.createTaskProgram(task, configuration)

                    val addTaskToStage = DSL.using(configuration)
                        .insertInto(tasksStageTable)
                        .columns(tasksStageTable.STAGE_ID, tasksStageTable.TASK_ID)
                        .values(
                            UUID.fromString(newStage[CommonFields.ID.value].toString()),
                            UUID.fromString(newTask[CommonFields.ID.value].toString())
                        )
                        .execute()
                }

                val addStageToActivity = DSL.using(configuration)
                    .insertInto(activityStageTable)
                    .columns(activityStageTable.ACTIVITY_ID, activityStageTable.STAGE_ID)
                    .values(
                        UUID.fromString(newActivity[CommonFields.ID.value].toString()),
                        UUID.fromString(newStage[CommonFields.ID.value].toString())
                    )
                    .execute()
            }

            newActivity
        }

        return runBlocking {
            when (val cb = Either.catch { activityM.fix().unsafeRunSync() }) {
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
        val profileM = provider.tx { configuration ->

            val activityInfo = getActivityProgram(id, configuration)

            val formattedSchema =
                formattedSchemaContentService.getFormattedSchemaContentProgram(
                    configuration,
                    UUID.fromString(activityInfo[CommonFields.SCHEMA_CONTENT_ID.value].toString())
                )
                    .toMutableMap()

            for (key in activityInfo.keys) {
                formattedSchema[key] = activityInfo[key]
            }


            when (activityInfo[ActivityFields.STATUS.value].toString()) {
                convertActivityStatus(ActivityStatus.not_started) -> {}
                else -> (formattedSchema[CommonFields.FIELDS.value] as MutableList<Map<String, Any?>>).removeIf { field ->
                    field[CommonFields.NAME.value] == ActivityFields.STAGES.value
                }
            }

            formattedSchema
        }


        return runBlocking {
            when (val cb = Either.catch { profileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        val stageM = provider.tx { configuration ->

            val currentStatus = DSL.using(configuration)
                .selectFrom(activityTable)
                .where(activityTable.ID.eq(id))
                .fetchOne()
                .map {it[activityTable.STATUS]}

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<ActivityRecord>>(
                DSL.using(configuration).update(activityTable)
            ) { statement, key ->
                when (val key = key.toString()) {
                    ActivityFields.NAME.value ->
                        statement.set(activityTable.NAME, body[key].`as`<String>())
                    ActivityFields.DESCRIPTION.value ->
                        statement.set(activityTable.DESCRIPTION, body[key].`as`<String>())
                    ActivityFields.FACULTY.value ->
                        statement.set(activityTable.FACULTY, convertFaculty(body[key].`as`<String>()))
                    ActivityFields.YEAR.value ->
                        statement.set(activityTable.YEAR, body[key].`as`<Int>())
                    ActivityFields.COURSE.value ->
                        statement.set(activityTable.COURSE, body[key].`as`<Int>())
                    ActivityFields.STATUS.value -> {
                        statement.set(activityTable.STATUS, convertActivityStatus(body[key].`as`<String>()))
                    }
                    ActivityFields.STAGES.value -> {
                        val newStatus = convertActivityStatus(body[ActivityFields.STATUS.value].`as`<String>())
                        if (currentStatus == ActivityStatus.not_started) {
                            updateStages(id, body, configuration)
                        } else {
                            if (newStatus == ActivityStatus.not_started) {
                                if (DSL.using(configuration)
                                        .selectFrom(Tables.STUDENT_GRADES)
                                        .where(Tables.STUDENT_GRADES.ACTIVITY_ID.eq(id))
                                        .fetchAny() != null
                                ) {
                                    statement.set(activityTable.STATUS, currentStatus)
                                } else {
                                }
                            } else {
                            }
                        }

                        statement.set(activityTable.ID, activityTable.ID)
                    }
                    else ->
                        statement
                }
            }

            when (updateStatement) {
                is UpdateSetMoreStep ->
                    updateStatement
                        .where(activityTable.ID.eq(id))
                        .returning()
                        .fetchOptional()
                        .map { record ->
                            CommonFields.ID.value to record[activityTable.ID]
                        }

                is UpdateSetFirstStep ->
                    Optional.empty()

                else ->
                    Optional.empty()
            }
        }

        val stage = runBlocking {
            when (val cb = Either.catch { stageM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (stage.isPresent) {
            false -> throw BadRequestException("Activity with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(stage.get()))
        }
    }

    override fun delete(id: UUID): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun getActivityProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        val activity = activitiesService.getActivityProgram(id, configuration).firstOrNull() as MutableMap

        val stagesIds = DSL.using(configuration)
            .select(activityStageTable.STAGE_ID)
            .from(activityStageTable)
            .innerJoin(stagesTable)
            .on(stagesTable.ID.eq(activityStageTable.STAGE_ID))
            .where(activityStageTable.ACTIVITY_ID.eq(id))
            .orderBy(stagesTable.STAGE_NUMBER.asc())
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[activityStageTable.STAGE_ID])

                list
            }

        val stages = ArrayList<Map<String, Any?>?>()

        for (stageId in stagesIds) {
            val stage = stagesService.getStageProgram(stageId, configuration).firstOrNull() as MutableMap

            val tasksIds = DSL.using(configuration)
                .select(tasksStageTable.TASK_ID)
                .from(tasksStageTable)
                .where(tasksStageTable.STAGE_ID.eq(stageId))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[tasksStageTable.TASK_ID])

                    list
                }

            val tasks = ArrayList<Map<String, Any?>?>()
            for (taskId in tasksIds) {
                tasks.add(tasksService.getTaskProgram(taskId, configuration).firstOrNull())
            }

            stage[ActivityFields.TASKS.value] = tasks
            stages.add(stage)
        }

        activity[ActivityFields.STAGES.value] = stages

        return activity
    }


    private fun updateStages(activityId: UUID, body: com.jsoniter.any.Any, configuration: Configuration) {

        val stagesToDelete =
            DSL.using(configuration)
                .deleteFrom(activityStageTable)
                .where(activityStageTable.ACTIVITY_ID.eq(activityId))
                .returning()
                .fetch()
                .fold (mutableListOf<UUID>()) { list, record ->
                    list.add(record[activityStageTable.STAGE_ID])
                    list
                }

        for (stageId in stagesToDelete) {
            val tasksToDelete =
                DSL.using(configuration)
                    .deleteFrom(tasksStageTable)
                    .where(tasksStageTable.STAGE_ID.eq(stageId))
                    .returning()
                    .fetch()
                    .fold(mutableListOf<UUID>()) { list, record ->
                        list.add(record[tasksStageTable.TASK_ID])
                        list
                    }

            for (taskId in tasksToDelete) {
                DSL.using(configuration)
                    .deleteFrom(tasksTable)
                    .where(tasksTable.ID.eq(taskId))
                    .execute()
            }

            DSL.using(configuration)
                .deleteFrom(stagesTable)
                .where(stagesTable.ID.eq(stageId))
                .execute()
        }


        val stages = body[ActivityFields.STAGES.value]

        for(stage in stages) {
            val newStage = stagesService.createStageProgram(stage, configuration)

            val tasks = stage[ActivityFields.TASKS.value]

            for(task in tasks) {
                val newTask = tasksService.createTaskProgram(task, configuration)

                val addTaskToStage = DSL.using(configuration)
                    .insertInto(tasksStageTable)
                    .columns(tasksStageTable.STAGE_ID, tasksStageTable.TASK_ID)
                    .values(
                        UUID.fromString(newStage[CommonFields.ID.value].toString()),
                        UUID.fromString(newTask[CommonFields.ID.value].toString())
                    )
                    .execute()
            }

            val addStageToActivity = DSL.using(configuration)
                .insertInto(activityStageTable)
                .columns(activityStageTable.ACTIVITY_ID, activityStageTable.STAGE_ID)
                .values(
                    activityId,
                    UUID.fromString(newStage[CommonFields.ID.value].toString())
                )
                .execute()
        }
    }
}