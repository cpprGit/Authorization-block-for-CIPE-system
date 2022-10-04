package ru.hse.cppr.service

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.*
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.*
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.formats.MailGroupHistoryFormat
import ru.hse.cppr.service.crud.AttributesDictionaryService
import ru.hse.cppr.service.crud.formatted.FormattedMailGroupService
import ru.hse.cppr.service.crud.formatted.FormattedProjectRequestService
import ru.hse.cppr.service.crud.formatted.FormattedProjectService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService
import ru.hse.cppr.service.notifications.Notification
import ru.hse.cppr.service.notifications.NotificationActions
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.service.profile.UserProfileService
import ru.hse.cppr.utils.*
import java.util.*

object FormattedService: KoinComponent {

    private val attributesDictionaryService: AttributesDictionaryService            by inject()

    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    val provider: TxProvider<ForIO>                                                 by inject()

    private val formattedProjectRequestService: FormattedProjectRequestService      by inject()
    private val formattedSchemaService: FormattedSchemaService                      by inject()
    private val formattedMailGroupService: FormattedMailGroupService                by inject()
    private val formattedProjectService: FormattedProjectService                    by inject()
    private val userProfileService: UserProfileService                              by inject()
    private val notificationService: NotificationsService                           by inject()
    private val mailGroupHistoryService: MailGroupHistoryService                    by inject()

    private val attrTable = Tables.ATTRIBUTES
    private val schemasTable = Tables.SCHEMAS_DICTIONARY
    private val usersTable = Tables.USERS
    private val projectRequestTable = Tables.PROJECT_REQUESTS
    private val projectsTable = Tables.PROJECTS
    private val activityStudent = Tables.ACTIVITY_STUDENT
    private val activityTable = Tables.ACTIVITY
    private val projectStudents = Tables.PROJECT_STUDENTS
    private val studentProjectRequest = Tables.STUDENT_PROJECT_REQUEST
    private val studentInfo = Tables.STUDENT_INFO
    private val studentInfoStudents = Tables.STUDENT_INFO_STUDENTS
    private val userMailGroupTable = Tables.USER_MAIL_GROUP
    private val mailGroupTable = Tables.MAIL_GROUP

    private val organisationsTable = Tables.ORGANISATIONS
    private val organisationUser = Tables.ORGANISATION_USER
    private val complaintsTable = Tables.USER_COMPLAINTS

    private val activityStudents = Tables.ACTIVITY_STUDENT


    fun startNewAcademicYear(): Map<String, String> {
        val txResult = provider.tx { configuration ->

            DSL.using(configuration)
                .update(studentInfo)
                .set(studentInfo.STATUS, StudentStatus.graduated)
                .where(studentInfo.COURSE.eq(4)
                    .and(studentInfo.STATUS.eq(StudentStatus.active)))
                .execute()

            DSL.using(configuration)
                .update(studentInfo)
                .set(studentInfo.COURSE, studentInfo.COURSE + 1)
                .where(studentInfo.COURSE.notEqual(4))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun addStudentToActivity(activityName: String, studentId: UUID): Map<String, String> {
        val txResult = provider.tx { configuration ->

            val activityId =
                DSL.using(configuration)
                    .selectFrom(activityTable)
                    .where(activityTable.NAME.eq(activityName))
                    .fetchOne()
                    .map {it[activityTable.ID]}

            DSL.using(configuration)
                .insertInto(activityStudents)
                .columns(activityStudents.ACTIVITY_ID, activityStudents.STUDENT_ID)
                .values(activityId, studentId)
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun getMentorActivities(id: UUID): List<Map<String, Any>> {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .distinctOn(activityTable.ID)
                .from(projectsTable)
                .innerJoin(usersTable)
                .on(projectsTable.LEADER_ID.eq(usersTable.ID))
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .where(usersTable.ID.eq(id))
                .fetch()
                .fold (mutableListOf<Map<String, Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[activityTable.ID].toString(),
                            CommonFields.NAME.value to record[activityTable.NAME].toString(),
                            CommonFields.TYPE.value to ProfileTypes.ACTIVITY.value
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun getMentorStudents(id: UUID): List<Map<String, Any>> {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projectsTable)
                .innerJoin(projectStudents)
                .on(projectStudents.PROJECT_ID.eq(projectsTable.ID))
                .innerJoin(usersTable)
                .on(usersTable.ID.eq(projectStudents.STUDENT_ID))
                .where(projectsTable.LEADER_ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[usersTable.ID].toString(),
                            CommonFields.NAME.value to record[usersTable.NAME],
                            CommonFields.TYPE.value to ProfileTypes.USER.value
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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



    fun getStudentMentors(id: UUID): List<Map<String, Any>> {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projectStudents)
                .innerJoin(projectsTable)
                .on(projectsTable.ID.eq(projectStudents.PROJECT_ID))
                .innerJoin(usersTable)
                .on(usersTable.ID.eq(projectsTable.LEADER_ID))
                .where(projectStudents.STUDENT_ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[usersTable.ID].toString(),
                            CommonFields.NAME.value to record[usersTable.NAME],
                            CommonFields.TYPE.value to ProfileTypes.USER.value
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun setComplaintViewed(id: UUID, status: String): Map<String, Any> {
        val txResult = provider.tx { configuration ->

            DSL.using(configuration)
                .update(complaintsTable)
                .set(complaintsTable.IS_VIEWED, status.toBoolean())
                .where(complaintsTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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



    fun getComplaintsList(): List<Map<String, Any>> {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(complaintsTable)
                .innerJoin(usersTable)
                .on(complaintsTable.CREATED_BY.eq(usersTable.ID))
                .orderBy(complaintsTable.IS_VIEWED.asc())
                .fetch()
                .fold(mutableListOf<Map<String, Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[complaintsTable.ID].toString(),
                            CommonFields.CREATED_BY.value to mapOf(
                                CommonFields.ID.value to record[complaintsTable.CREATED_BY].toString(),
                                CommonFields.NAME.value to record[usersTable.NAME],
                                CommonFields.TYPE.value to ProfileTypes.USER.value
                            ),
                            ReportFields.PROFILE.value to mapOf(
                                CommonFields.ID.value to record[complaintsTable.PROFILE_ID].toString(),
                                CommonFields.NAME.value to record[complaintsTable.PROFILE_NAME],
                                CommonFields.TYPE.value to record[complaintsTable.PROFILE_TYPE]
                            ),
                            ReportFields.IS_VIEWED.value to record[complaintsTable.IS_VIEWED],
                            ReportFields.COMPLAINT.value to record[complaintsTable.COMPLAINT]
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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

    fun getEntityName(id: UUID, type: String, configuration: Configuration): String {
        return when (type) {
            ProfileTypes.USER.value -> {
                DSL.using(configuration)
                    .selectFrom(usersTable)
                    .where(usersTable.ID.eq(id))
                    .fetchOne()
                    .map {it[usersTable.NAME]}
            }
            else  -> {
                DSL.using(configuration)
                    .selectFrom(organisationsTable)
                    .where(organisationsTable.ID.eq(id))
                    .fetchOne()
                    .map {it[organisationsTable.NAME]}
            }
        }
    }


    fun createComplaint(body: com.jsoniter.any.Any): Map<String, String> {
        val txResult = provider.tx { configuration ->

            val profileType = body[ReportFields.PROFILE.value][CommonFields.TYPE.value].toString()
            val profileId = UUID.fromString(body[ReportFields.PROFILE.value][CommonFields.ID.value].toString())

            DSL.using(configuration)
                .insertInto(complaintsTable)
                .columns(
                    complaintsTable.CREATED_BY,
                    complaintsTable.PROFILE_ID,
                    complaintsTable.PROFILE_NAME,
                    complaintsTable.PROFILE_TYPE,
                    complaintsTable.COMPLAINT
                )
                .values(
                    UUID.fromString(body[CommonFields.CREATED_BY.value].toString()),
                    profileId,
                    getEntityName(profileId, profileType, configuration),
                    profileType,
                    body[ReportFields.COMPLAINT.value].toString()
                )
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun setOrganisationBlocked(id: UUID, isBlocked: String): Map<String, String> {
        val status = when (isBlocked.toBoolean()) {
            true -> BlockedStatus.blocked
            false -> BlockedStatus.active
        }

        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .update(organisationsTable)
                .set(organisationsTable.BLOCKED_STATUS, status)
                .where(organisationsTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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



    fun sendTextNotificationToMailGroup(id: UUID, body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val notificationSenderId =
                DSL.using(configuration)
                    .selectFrom(mailGroupTable)
                    .where(mailGroupTable.ID.eq(id))
                    .fetchOne()
                    .map { it[mailGroupTable.CREATED_BY] }

            val userIds =
                DSL.using(configuration)
                    .selectFrom(userMailGroupTable)
                    .where(userMailGroupTable.MAIL_GROUP_ID.eq(id))
                    .fetch()
                    .fold(mutableListOf<UUID>()) { list, record ->
                        list.add(record[userMailGroupTable.USER_ID])

                        list
                    }

            for (userId in userIds) {
                val notification =
                    Notification(userId, notificationSenderId, NotificationActions.SEND_MESSAGE, NotificationType.all)
                notification.text = body[MailGroupHistoryFields.MESSAGE.value].toString()
                notificationService.createNotification(notification, configuration)
            }

            mailGroupHistoryService.createRecordProgram(id, MailGroupHistoryFormat(body), configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun setUserBlocked(id: UUID, isBlocked: String): Map<String, String> {
        val status = when (isBlocked.toBoolean()) {
            true -> BlockedStatus.blocked
            false -> BlockedStatus.active
        }

        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .update(usersTable)
                .set(usersTable.STATUS, status)
                .where(usersTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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

    fun setProjectRequestStatus(id: UUID, status: String): Map<String, String> {
        val value = when (status.toBoolean()) {
            true -> ProjectRequestStatus.pending
            false -> ProjectRequestStatus.declined

        }

        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .update(projectRequestTable)
                .set(projectRequestTable.STATUS, value)
                .where(projectRequestTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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

    fun setStudentStatus(id: UUID, status: String): Map<String, String> {
        val txResult = provider.tx { configuration ->
            val sisId =
                DSL.using(configuration)
                    .selectFrom(studentInfoStudents)
                    .where(studentInfoStudents.STUDENT_ID.eq(id))
                    .fetchOne()
                    .map {it[studentInfoStudents.STUDENT_INFO_ID]}

            DSL.using(configuration)
                .update(studentInfo)
                .set(studentInfo.STATUS, convertStudentStatus(status))
                .where(studentInfo.ID.eq(sisId))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun getOrganisationEmployers(id: UUID): List<Map<String, Any?>> {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(usersTable)
                .innerJoin(organisationUser)
                .on(usersTable.ID.eq(organisationUser.USER_ID))
                .where(organisationUser.ORGANISATION_ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, Any>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[usersTable.ID].toString(),
                            CommonFields.NAME.value to record[usersTable.NAME],
                            CommonFields.TYPE.value to ProfileTypes.USER.value,
                            UserFields.ROLE.value to record[usersTable.TYPE]
                        )
                    )
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun setSchemaArchived(id: UUID, archived: Boolean):  Map<String, String> {
        val txResult = provider.tx { configuration ->

            DSL.using(configuration)
                .update(schemasTable)
                .set(schemasTable.ARCHIVED, archived)
                .where(schemasTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }


        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    fun getArchivedSchemas(): ArrayList<Map<String, Any?>> {
        val txResult = provider.tx { configuration ->
            val schemasIds = DSL.using(configuration)
                .select(schemasTable.ID)
                .from(schemasTable)
                .where(schemasTable.SCHEMA_TYPE.notEqual(SchemaType.student_profile)
                    .and(schemasTable.SCHEMA_TYPE.notEqual(SchemaType.user_profile))
                    .and(schemasTable.SCHEMA_TYPE.notEqual(SchemaType.project))
                    .and(schemasTable.ARCHIVED.eq(true)))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[schemasTable.ID])

                    list
                }

            val schemas = ArrayList<Map<String, Any?>>()

            for (schemaId in schemasIds) {
                schemas.add(formattedSchemaService.getFormattedSchemaProgram(configuration, schemaId))
            }

            schemas
        }


        return runBlocking {
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
    }

    fun openCloseProjectApplyPeriod(activityName: String, status: Boolean):  Map<String, String> {
        val programM = provider.tx { configuration ->

            val activityStatus = when (status) {
                true -> ActivityStatus.apply_open
                false -> ActivityStatus.not_started
            }

            DSL.using(configuration)
                .update(activityTable)
                .set(activityTable.STATUS, activityStatus)
                .where(activityTable.NAME.eq(activityName))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun setActivityStatus(id: UUID, body: com.jsoniter.any.Any): Map<String, String> {
        val programM = provider.tx { configuration ->
            val status = convertActivityStatus(body[ActivityFields.STATUS.value].toString())
            DSL.using(configuration)
                .update(activityTable)
                .set(activityTable.STATUS, status)
                .where(activityTable.ID.eq(id))
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getAllNotFinishedActivities(): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(activityTable)
                .where(activityTable.STATUS.notEqual(ActivityStatus.finished)
                    .and(activityTable.ID.notEqual(UUID.fromString("00000000-0000-0000-0000-000000000000"))))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapOf(
                        ActivityFields.ID.value to record[activityTable.ID].toString(),
                        ActivityFields.NAME.value to record[activityTable.NAME]
                    ))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getActivityProjectsByStatus(id: UUID, status: ActivityStatus): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projectsTable)
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .where(activityTable.STATUS.eq(status).and(activityTable.ID.eq(id)))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapProjectInfo(record, configuration))
                    list
                }
        }


        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getAllActivityProjectsForApply(id: UUID, userId: UUID?): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projectsTable)
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .where(activityTable.ID.eq(id)
                    .and(activityTable.STATUS.eq(ActivityStatus.apply_open)))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    val projectM = mapProjectInfo(record, configuration) as MutableMap<String, Any?>
                    projectM["isApplied"] = false

                    if (userId != null ) {
                        val applied = DSL.using(configuration)
                            .selectFrom(studentProjectRequest)
                            .where(
                                studentProjectRequest.STUDENT_ID.eq(userId)
                                    .and(studentProjectRequest.PROJECT_ID.eq(record[projectsTable.ID]))
                            )
                            .fold(mutableListOf<UUID>()) { list, record ->
                                list.add(record[projectsTable.ID])
                                list
                            }

                        if (applied.isNotEmpty()) {
                            projectM["isApplied"] = true
                        }
                    }

                    val isStudentAlreadyOnThisProject = DSL.using(configuration)
                        .selectFrom(projectStudents)
                        .where(projectStudents.PROJECT_ID.eq(UUID.fromString(projectM[CommonFields.ID.value].toString()))
                            .and(projectStudents.STUDENT_ID.eq(userId)))
                        .fetchAny() != null

                    if (!isStudentAlreadyOnThisProject) {
                        list.add(projectM)
                    }

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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



    fun getProjectStudents(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(projectStudents)
                .innerJoin(projectsTable)
                .on(projectStudents.PROJECT_ID.eq(projectsTable.ID))
                .innerJoin(usersTable)
                .on(usersTable.ID.eq(projectStudents.STUDENT_ID))
                .innerJoin(studentInfoStudents)
                .on(projectStudents.STUDENT_ID.eq(studentInfoStudents.STUDENT_ID))
                .innerJoin(studentInfo)
                .on(studentInfoStudents.STUDENT_INFO_ID.eq(studentInfo.ID))
                .where(
                    projectStudents.PROJECT_ID.eq(id)
                )
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapOf(
                        StudentFields.ID.value to record[usersTable.ID].toString(),
                        StudentFields.NAME.value to mapOf(
                            CommonFields.ID.value to record[usersTable.ID].toString(),
                            CommonFields.NAME.value to record[usersTable.NAME],
                            CommonFields.TYPE.value to ProfileTypes.USER.value
                        ),
                        StudentFields.ROLE.value to record[usersTable.TYPE],
                        StudentFields.COURSE.value to record[studentInfo.COURSE],
                        StudentFields.FACULTY.value to record[studentInfo.FACULTY],
                        StudentFields.GROUP.value to record[studentInfo.GROUP_NAME],
                        StudentFields.EMAIL.value to record[studentInfo.EMAIL],
                        StudentFields.STATUS.value to record[studentInfo.STATUS]
                    ))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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



    fun getProjectAppliedStudents(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(studentProjectRequest)
                .innerJoin(projectsTable)
                .on(studentProjectRequest.PROJECT_ID.eq(projectsTable.ID))
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .innerJoin(usersTable)
                .on(usersTable.ID.eq(studentProjectRequest.STUDENT_ID))
                .innerJoin(studentInfoStudents)
                .on(studentProjectRequest.STUDENT_ID.eq(studentInfoStudents.STUDENT_ID))
                .innerJoin(studentInfo)
                .on(studentInfoStudents.STUDENT_INFO_ID.eq(studentInfo.ID))
                .where(
                    studentProjectRequest.PROJECT_ID.eq(id)
                        .and(activityTable.STATUS.eq(ActivityStatus.apply_open))
                )
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapOf(
                        StudentFields.ID.value to record[usersTable.ID].toString(),
                        StudentFields.NAME.value to mapOf(
                            CommonFields.ID.value to record[usersTable.ID].toString(),
                            CommonFields.NAME.value to record[usersTable.NAME],
                            CommonFields.TYPE.value to ProfileTypes.USER.value
                        ),
                        StudentFields.ROLE.value to record[usersTable.TYPE],
                        StudentFields.COURSE.value to record[studentInfo.COURSE],
                        StudentFields.FACULTY.value to record[studentInfo.FACULTY],
                        StudentFields.GROUP.value to record[studentInfo.GROUP_NAME],
                        StudentFields.EMAIL.value to record[studentInfo.EMAIL],
                        StudentFields.STATUS.value to record[studentInfo.STATUS]
                    ))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getStudentAppliedProjects(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(studentProjectRequest)
                .innerJoin(projectsTable)
                .on(studentProjectRequest.PROJECT_ID.eq(projectsTable.ID))
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .where(
                    studentProjectRequest.STUDENT_ID.eq(id)
                        .and(activityTable.STATUS.eq(ActivityStatus.apply_open))
                )
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapProjectInfo(record, configuration))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


        fun getStudentActiveProjects(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->

            DSL.using(configuration)
                .select()
                .from(projectStudents)
                .innerJoin(projectsTable)
                .on(projectStudents.PROJECT_ID.eq(projectsTable.ID))
                .innerJoin(activityTable)
                .on(projectsTable.ACTIVITY_ID.eq(activityTable.ID))
                .where(
                    projectStudents.STUDENT_ID.eq(id)
                        .and(activityTable.STATUS.eq(ActivityStatus.started))
                            .or(activityTable.STATUS.eq(ActivityStatus.apply_open))
                )
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapProjectInfo(record, configuration))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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

    fun getStudentActivities(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->
            val student = userProfileService.getUserInfo(UserType.student, id, configuration)

            val studentsCourse = student[StudentFields.COURSE.value].toString().toInt()
            val studentsFaculty = convertFaculty(student[StudentFields.FACULTY.value].toString())

            val validActivitiesForStudent =
                DSL.using(configuration)
                    .selectFrom(activityTable)
                    .where(
                        activityTable.COURSE.eq(studentsCourse)
                            .and(activityTable.FACULTY.eq(studentsFaculty))
                            .and(
                                activityTable.STATUS.eq(ActivityStatus.started)
                                    .or(activityTable.STATUS.eq(ActivityStatus.apply_open))
                            )
                    )
                    .fetch()
                    .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                        list.add(
                            mapOf(
                                ActivityFields.NAME.value to mapOf(
                                    CommonFields.ID.value to record[activityTable.ID].toString(),
                                    CommonFields.NAME.value to record[activityTable.NAME].toString(),
                                    CommonFields.TYPE.value to ProfileTypes.ACTIVITY.value
                                ),
                                ActivityFields.FACULTY.value to convertFaculty(
                                    FacultyType.valueOf(record[activityTable.FACULTY].toString())
                                ),
                                ActivityFields.COURSE.value to record[activityTable.COURSE],
                                ActivityFields.DESCRIPTION.value to record[activityTable.DESCRIPTION],
                                ActivityFields.STATUS.value to record[activityTable.STATUS],
                                ActivityFields.YEAR.value to record[activityTable.YEAR]
                            )
                        )
                        list
                    }

            val extraActivitiesForStudent =
                DSL.using(configuration)
                    .select(
                        activityTable.ID,
                        activityTable.NAME,
                        activityTable.FACULTY,
                        activityTable.COURSE,
                        activityTable.DESCRIPTION,
                        activityTable.STATUS,
                        activityTable.YEAR
                    )
                    .from(activityTable)
                    .innerJoin(activityStudent)
                    .on(activityTable.ID.eq(activityStudent.ACTIVITY_ID))
                    .where(activityStudent.STUDENT_ID.eq(id))
                    .fetch()
                    .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                        list.add(
                            mapOf(
                                ActivityFields.NAME.value to mapOf(
                                    CommonFields.ID.value to record[activityTable.ID].toString(),
                                    CommonFields.NAME.value to record[activityTable.NAME].toString(),
                                    CommonFields.TYPE.value to ProfileTypes.ACTIVITY.value
                                ),
                                ActivityFields.FACULTY.value to convertFaculty(
                                    FacultyType.valueOf(record[activityTable.FACULTY].toString())
                                ),
                                ActivityFields.COURSE.value to record[activityTable.COURSE],
                                ActivityFields.DESCRIPTION.value to record[activityTable.DESCRIPTION],
                                ActivityFields.STATUS.value to record[activityTable.STATUS],
                                ActivityFields.YEAR.value to record[activityTable.YEAR]
                            )
                        )
                        list
                    }

            if (extraActivitiesForStudent.isNotEmpty()) {
                for (activity in extraActivitiesForStudent) {
                    validActivitiesForStudent.add(activity)
                }
            }

            validActivitiesForStudent.distinctBy { it[ActivityFields.NAME.value]} as MutableList
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getAllMentorsProjects(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->

            DSL.using(configuration)
                .selectFrom(projectsTable)
                .where(projectsTable.LEADER_ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(mapProjectInfo(record, configuration))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun getAllMentorsProjectRequests(id: UUID): MutableList<Map<String, Any?>> {
        val programM = provider.tx { configuration ->

            DSL.using(configuration)
                .selectFrom(projectRequestTable)
                .where(projectRequestTable.LEADER_ID.eq(id)
                        .or(projectRequestTable.CREATED_BY.eq(id)))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(
                        mapOf(
                            ProjectFields.ID.value to record[projectRequestTable.ID].toString(),
                            ProjectFields.PROJECT_NAME_RUS.value to record[projectRequestTable.NAME_RUS].toString(),
                            ProjectFields.PROJECT_NAME_ENG.value to record[projectRequestTable.NAME_ENG].toString(),
                            ProjectFields.STATUS.value to convertProjectRequestStatus(record[projectRequestTable.STATUS])
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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



    fun getAllUsersForMailGroups(): MutableMap<String, Any?> {
        val programM = provider.tx { configuration ->

            val users = DSL.using(configuration)
                .selectFrom(usersTable)
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    list.add(userProfileService.getUserInfo(
                        record[usersTable.TYPE],
                        record[usersTable.ID],
                        configuration
                    ))

                    list
                }

            val result = mutableMapOf<String, Any?>()

            result[MailGroupFields.USERS.value] = users
            result[MailGroupFields.MAIL_FIELDS.value] = formattedMailGroupService.addMailGroupFields()

            result
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun acceptProjectRequest(id: UUID): Map<String, Any?> {
        val programM = provider.tx { configuration ->

            val projectRequest = formattedProjectRequestService.getProjectRequestProfileProgram(id, configuration)

            if (projectRequest[ProjectFields.STATUS.value].toString()
                != convertProjectRequestStatus(ProjectRequestStatus.accepted)
            ) {

                DSL.using(configuration)
                    .update(projectRequestTable)
                    .set(projectRequestTable.STATUS, ProjectRequestStatus.accepted)
                    .where(projectRequestTable.ID.eq(id))
                    .execute()

                projectRequest[ProjectFields.STATUS.value] = ProjectRequestStatus.accepted.toString()

                val newProjectBody = JsonIterator.deserialize(
                    JsonStream.serialize(
                        com.jsoniter.any.Any.wrap(projectRequest)
                    )
                )


                formattedProjectService.createProjectProgram(newProjectBody, configuration)
            } else {
                mapOf("status" to "success")
            }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun rejectProjectRequest(id: UUID) {
        val programM = provider.tx { configuration ->

            val projectRequest = formattedProjectRequestService.getProjectRequestProfileProgram(id, configuration)

            when (convertProjectRequestStatus(projectRequest[ProjectFields.STATUS.value].toString())) {
                ProjectRequestStatus.pending ->
                    DSL.using(configuration)
                        .update(projectRequestTable)
                        .set(projectRequestTable.STATUS, ProjectRequestStatus.declined)
                        .where(projectRequestTable.ID.eq(id))
                        .execute()
                else -> {
                } //TODO: handle other cases
            }
        }

        runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
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


    fun listAttributesByUsageName(usageName: String): ArrayList<MutableMap<String, Any?>> {
        val attributes = ArrayList<MutableMap<String, Any?>>()

        val attributesList = attributesDictionaryService.list()
        for (attr in attributesList) {

            if (attr[attrTable.USAGE.name]?.equals(usageName)!!) {
                attributes.add(attr as MutableMap<String, Any?>)
            }
        }

        return attributes
    }


    private fun mapProjectInfo(record: Record, configuration: Configuration): Map<String, Any?> {
        return mapOf(
            ProjectFields.ID.value to record[projectsTable.ID].toString(),
            ProjectFields.PROJECT_NAME_RUS.value to mapOf(
                "id" to record[projectsTable.ID]?.toString(),
                "name" to record[projectsTable.NAME_RUS]?.toString(),
                "type" to "project"
            ),
            ProjectFields.PROJECT_NAME_ENG.value to mapOf(
                "id" to record[projectsTable.ID]?.toString(),
                "name" to record[projectsTable.NAME_ENG]?.toString(),
                "type" to "project"
            ),
            ProjectFields.PROJECT_TYPE.value to convertProjectType(record[projectsTable.TYPE]),
            ProjectFields.PROJECT_INDIVIDUALITY.value to convertProjectIndividuality(record[projectsTable.IS_GROUP_PROJECT]),
            ProjectFields.MENTOR.value to getUser(
                record[projectsTable.LEADER_ID],
                configuration
            ),
            ProjectFields.CONSULTANT.value to getUser(
                record[projectsTable.CONSULTANT_ID],
                configuration
            ),
            ProjectFields.ACTIVITY.value to getActivity(
                record[projectsTable.ACTIVITY_ID].toString(),
                configuration
            ),
            ProjectFields.PI_COURSES.value to record[projectsTable.PI]?.toString(),
            ProjectFields.PMI_COURSES.value to record[projectsTable.PMI]?.toString(),
            ProjectFields.PAD_COURSES.value to record[projectsTable.PAD]?.toString()
        )
    }


}
