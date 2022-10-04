package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.ActivityFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.service.crud.ProjectService
import ru.hse.cppr.service.crud.SchemaContentService
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.utils.*
import java.util.*

class FormattedProjectService(override val serviceName: String): KoinComponent, CRUDService {

    val provider: TxProvider<ForIO>                                             by inject()
    val log: Log                                                                by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }


    private val schemaContentService: SchemaContentService                              by inject()
    private val projectsService: ProjectService                                         by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService            by inject()
    private val postsService: PostsService                                              by inject()
    private val formattedActivitiesService: FormattedActivitiesService                  by inject()


    private val users = Tables.USERS
    private val projects = Tables.PROJECTS
    private val schemaContent = Tables.SCHEMA_CONTENT
    private val activitiesTable = Tables.ACTIVITY



    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val projectM = provider.tx { configuration ->
            createProjectProgram(body, configuration)
        }

        val res = runBlocking {
            when (val cb = Either.catch { projectM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return res
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: UUID): Map<String, Any?> {
        val profileM = provider.tx { configuration ->

            val project = projectsService.getProjectProgram(id, configuration)

            val formattedSchema =
                formattedSchemaContentService.getFormattedSchemaContentProgram(
                    configuration,
                    UUID.fromString(project[CommonFields.SCHEMA_CONTENT_ID.value].toString())
                )
                    .toMutableMap()

            for (key in project.keys) {
                when (key) {
                    ProjectFields.ACTIVITY_ID.value -> formattedSchema[ProjectFields.ACTIVITY.value] =
                        getActivity(project[key].toString(), configuration)
                    ProjectFields.MENTOR_ID.value -> formattedSchema[ProjectFields.MENTOR.value] =
                        getUser(UUID.fromString(project[key].toString()), configuration)
                    ProjectFields.CONSULTANT_ID.value -> formattedSchema[ProjectFields.CONSULTANT.value] =
                        getUser(getOptUUID(project[key]), configuration)
                    else -> formattedSchema[key] = project[key]
                }
            }

            formattedSchema[ActivityFields.STAGES.value] = getProjectStages(id, configuration)

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
        projectsService.update(id, body)

        val result = provider.tx { configuration ->

            val projectProfileId = DSL.using(configuration)
                .select(projects.SCHEMA_CONTENT_ID)
                .from(projects)
                .where(projects.ID.eq(id))
                .fetchOne()
                .let { it[projects.SCHEMA_CONTENT_ID] }

            val projectProfileUpdate = DSL.using(configuration)
                .update(schemaContent)
                .set(schemaContent.CONTENT, body[CommonFields.SCHEMA_CONTENT.value].toString())
                .where(schemaContent.ID.eq(projectProfileId))
                .execute()

            projectProfileUpdate
        }


        runBlocking {
            when (val cb = Either.catch { result.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return "{\"status\": \"Success\"}"
    }

    override fun delete(id: UUID): String {
        //TODO: add logic that is connected to deletion of the project, e.g. unlink students and mentors
        return projectsService.delete(id)
    }


    fun createProjectProgram(body: com.jsoniter.any.Any, configuration: Configuration): Map<String, Any?> {
        val createdProjectProfile = schemaContentService.createProgram(
            com.jsoniter.any.Any.wrap(
                mapOf(
                    CommonFields.SCHEMA_ID.value to CurrentSchemaService
                        .getCurrentSchemaProgram(SchemaType.project, configuration)
                        .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                    CommonFields.SCHEMA_CONTENT.value to body[CommonFields.SCHEMA_CONTENT.value].toString()
                )
            ),
            configuration
        )

        val activityID = when (val activityName = body.getOpt(ProjectFields.ACTIVITY.value)?.asOpt<String>()) {
            null, "" -> UUID.fromString("00000000-0000-0000-0000-000000000000")
            else -> DSL.using(configuration)
                .selectFrom(activitiesTable)
                .where(activitiesTable.NAME.eq(activityName))
                .fetchOne()
                .map { it[activitiesTable.ID] }
        }

        //TODO: replace with service method
        val createdProject = DSL.using(configuration)
            .insertInto(projectsService.table)
            .columns(projectsService.COLUMNS)
            .values(
                getOptUUID(CommonFields.ID.value, body[ProjectFields.CONSULTANT.value]),
                body[ProjectFields.MENTOR.value][CommonFields.ID.value].`as`<String>().let(UUID::fromString),
                activityID,
                createdProjectProfile[CommonFields.ID.value].toString().let(UUID::fromString),

                body[ProjectFields.PROJECT_NAME_RUS.value].`as`(),
                body.getOpt(ProjectFields.PROJECT_NAME_ENG.value)?.asOpt(),
                convertProjectIndividuality(body.getOpt(ProjectFields.PROJECT_INDIVIDUALITY.value)?.asOpt()),
                convertProjectType(body.getOpt(ProjectFields.PROJECT_TYPE.value)?.asOpt()).toString(),
                body.getOpt(ProjectFields.MAX_STUDENTS.value)?.asOpt(),

                body.getOpt(ProjectFields.PI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PMI_COURSES.value)?.asOpt(),
                body.getOpt(ProjectFields.PAD_COURSES.value)?.asOpt()
            )
            .returning(projectsService.COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                projectsService.fetchMapValues(it)
            }

        val leaderUUID = createdProject[ProjectFields.MENTOR_ID.value]
        val consultantUUID = createdProject[ProjectFields.CONSULTANT_ID.value]
        val activityUUID = createdProject[ProjectFields.ACTIVITY_ID.value]



        return createdProject
    }

    private fun getProjectStages(id: UUID, configuration: Configuration): Any? {
        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(id))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}

        return formattedActivitiesService.getActivityProgram(activityId, configuration)[ActivityFields.STAGES.value]
    }

}