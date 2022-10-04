package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.NotificationTargetType
import ru.hse.cppr.data.database_generated.enums.NotificationType
import ru.hse.cppr.data.database_generated.enums.ProjectRequestStatus
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.service.crud.ProjectRequestService
import ru.hse.cppr.service.crud.SchemaContentService
import ru.hse.cppr.service.notifications.Notification
import ru.hse.cppr.service.notifications.NotificationActions
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.utils.*
import java.util.*

class FormattedProjectRequestService(override val serviceName: String): KoinComponent, CRUDService {

    val provider: TxProvider<ForIO>                                             by inject()

    val log: Log                                                                by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val schemaContentService: SchemaContentService                      by inject()
    private val projectRequestService: ProjectRequestService                    by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService    by inject()
    private val postsService: PostsService                                      by inject()
    private val notificationService: NotificationsService                       by inject()

    private val usersTable = Tables.USERS
    private val projectRequestsTable = Tables.PROJECT_REQUESTS
    private val schemaContent = Tables.SCHEMA_CONTENT



    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val projectRequestM = provider.tx { configuration ->

            val createdProjectRequestProfile = schemaContentService.createProgram(
                com.jsoniter.any.Any.wrap(mapOf(
                    CommonFields.SCHEMA_ID.value to CurrentSchemaService
                        .getCurrentSchemaProgram(SchemaType.project_request, configuration)
                        .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                    CommonFields.SCHEMA_CONTENT.value to body[CommonFields.SCHEMA_CONTENT.value].toString()
                )),
                configuration
            )


            val createdProjectRequest = projectRequestService.createProjectRequestProgram(
                body,
                createdProjectRequestProfile[CommonFields.ID.value].toString(),
                configuration
            )

            val notificationReceivers = getManagersAndSupervisorIds(configuration)

            for (receiver in notificationReceivers) {
                val notification = Notification(
                    receiver,
                    getUUID(body[CommonFields.CREATED_BY.value]),
                    NotificationActions.CREATED_PROJECT_REQUEST,
                    NotificationType.profile
                )

                notification.targetId = UUID.fromString(createdProjectRequest[ProjectFields.ID.value].toString())
                notification.targetName = createdProjectRequest[ProjectFields.PROJECT_NAME_RUS.value].toString()
                notification.targetType = NotificationTargetType.project_request

                notificationService.createNotification(notification, configuration)
            }


            createdProjectRequest
        }

        val res = runBlocking {
            when (val cb = Either.catch { projectRequestM.fix().unsafeRunSync() }) {
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
        val projectRequestM = provider.tx { configuration ->
            getProjectRequestProfileProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { projectRequestM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        projectRequestService.update(id, body)

        val result = provider.tx { configuration ->

            val projectProfileId = DSL.using(configuration)
                .select(projectRequestsTable.SCHEMA_CONTENT_ID)
                .from(projectRequestsTable)
                .where(projectRequestsTable.ID.eq(id))
                .fetchOne()
                .let { it[projectRequestsTable.SCHEMA_CONTENT_ID] }

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
        val deleteM = provider.tx { configuration ->

            DSL.using(configuration)
                .delete(projectRequestsTable)
                .where(projectRequestsTable.ID.eq(id))
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

    fun getProjectRequestProfileProgram(id: UUID, configuration: Configuration): MutableMap<String, Any?> {
        val projectRequest = projectRequestService.getProjectRequestProgram(id, configuration)

        val formattedSchema =
            formattedSchemaContentService.getFormattedSchemaContentProgram(
                configuration,
                UUID.fromString(projectRequest[CommonFields.SCHEMA_CONTENT_ID.value].toString()))
                .toMutableMap()

        for (key in projectRequest.keys) {
            when(key) {
                ProjectFields.MENTOR_ID.value -> formattedSchema[ProjectFields.MENTOR.value] = getUser(UUID.fromString(projectRequest[key].toString()), configuration)
                ProjectFields.CONSULTANT_ID.value -> formattedSchema[ProjectFields.CONSULTANT.value] = getUser(getOptUUID(projectRequest[key]), configuration)
                ProjectFields.CREATED_BY.value -> formattedSchema[ProjectFields.CREATED_BY.value] = getUser(getOptUUID(projectRequest[key]), configuration)
                else -> formattedSchema[key] = projectRequest[key]
            }
        }

        formattedSchema[CommonFields.POSTS.value] = postsService.getPostsByProfileIdProgram(
            UUID.fromString(projectRequest[CommonFields.SCHEMA_CONTENT_ID.value].toString()),
            configuration
        )
        formattedSchema[CommonFields.MODIFY_ALLOWED.value] = true

        return formattedSchema
    }


    private fun getUser(id: UUID?, configuration: Configuration): Map<String, Any?>? {
        if (id == null) {
            return null
        }


        return DSL.using(configuration)
            .selectFrom(usersTable)
            .where(usersTable.ID.eq(id))
            .fetchOne()
            .map { record ->
                mapOf(
                    CommonFields.ID.value to record[usersTable.ID].toString(),
                    CommonFields.NAME.value to record[usersTable.NAME].toString(),
                    CommonFields.TYPE.value to ProfileTypes.USER.value
                )
            }
    }
}