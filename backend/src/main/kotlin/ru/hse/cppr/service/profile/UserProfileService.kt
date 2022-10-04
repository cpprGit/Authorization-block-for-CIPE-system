package ru.hse.cppr.service.profile

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.auth0.jwt.interfaces.DecodedJWT
import com.jsoniter.any.Any
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.TableField
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.NotificationTargetType
import ru.hse.cppr.data.database_generated.enums.NotificationType
import ru.hse.cppr.data.database_generated.enums.UserType
import ru.hse.cppr.data.database_generated.tables.records.*
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.representation.enums.fields.*
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.security.UserProfileSecurityProvider
import ru.hse.cppr.service.crud.formatted.FormattedSchemaContentService
import ru.hse.cppr.service.notifications.Notification
import ru.hse.cppr.service.notifications.NotificationActions
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.service.users.UsersService
import ru.hse.cppr.utils.*
import java.util.*

class UserProfileService(override val serviceName: String) : KoinComponent, ProfileService {
    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val provider: TxProvider<ForIO>                                         by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService        by inject()
    private val postsService: PostsService                                          by inject()
    private val usersService: UsersService                                          by inject()
    private val notificationsService: NotificationsService                          by inject()
    private val organisationRelationshipsService: OrganisationRelationshipsService  by inject()


    private val securityProvider = UserProfileSecurityProvider()

    private val schemaContent = Tables.SCHEMA_CONTENT
    private val studentInfo_students = Tables.STUDENT_INFO_STUDENTS
    private val studentInfo = Tables.STUDENT_INFO
    private val users = Tables.USERS
    private val organisationUsers = Tables.ORGANISATION_USER
    private val departmentUsers = Tables.DEPARTMENT_USER
    private val organisations = Tables.ORGANISATIONS


    override fun getProfile(id: UUID): Map<String, kotlin.Any?> {
        val profileM = provider.tx { configuration ->

            val user = usersService.getUser(id)
            val formattedSchema =
                    formattedSchemaContentService.getFormattedSchemaContentProgram(
                            configuration,
                            UUID.fromString(user["schema_content_id"].toString())
                    ).toMutableMap()

            val userInfo = getUserInfo(UserType.valueOf(user[UserFields.ROLE.value].toString()), id, configuration)

            for (key in userInfo.keys) {
                formattedSchema[key] = userInfo[key]
            }

            (formattedSchema[CommonFields.FIELDS.value] as MutableList<Map<String, kotlin.Any?>>).removeIf { field ->
                val attr = field[CommonFields.ATTRIBUTE.value] as Map<String, kotlin.Any?>
                (attr[CommonFields.USAGE.value].toString().toLowerCase() == CommonFields.PASSWORD.value) ||
                        (attr[CommonFields.NAME.value].toString() == "User Role")
            }

            formattedSchema[CommonFields.POSTS.value] = postsService.getPostsByProfileIdProgram(
                    UUID.fromString(user["schema_content_id"].toString()),
                    configuration
            )

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

    override fun getProfile(id: UUID, jwt: DecodedJWT): Map<String, kotlin.Any?> {
        val profile = getProfile(id) as MutableMap
        profile[CommonFields.MODIFY_ALLOWED.value] = securityProvider.isModifyAllowed(id, jwt)

        return profile
    }


    override fun editProfile(id: UUID, body: Any, jwt: DecodedJWT) {
        securityProvider.checkModifyAllowed(id, jwt)

        val result = provider.tx { configuration ->
            val userProfileId = DSL.using(configuration)
                .select()
                .from(users)
                .where(users.ID.eq(id))
                .fetchOne()
                .let { it[users.SCHEMA_CONTENT_ID] }

            val userProfileUpdate = DSL.using(configuration)
                .update(schemaContent)
                .set(schemaContent.CONTENT, body[CommonFields.SCHEMA_CONTENT.value].toString())
                .where(schemaContent.ID.eq(userProfileId))
                .execute()

            editDefaultAttributes(id, body, configuration)


            when (jwt.getClaim(JwtClaims.ROLE.value).asString()) {
                UserRoles.MENTOR.value,
                UserRoles.REPRESENTATIVE.value -> {
                    val notificationReceivers = getManagersAndSupervisorIds(configuration)

                    for (receiver in notificationReceivers) {
                        val notification = Notification(
                            receiver,
                            UUID.fromString(jwt.getClaim(JwtClaims.ID.value).asString()),
                            NotificationActions.UPDATED_PROFILE,
                            NotificationType.profile
                        )

                        notification.targetId = UUID.fromString(jwt.getClaim(JwtClaims.ID.value).asString())
                        notification.targetName = jwt.getClaim(JwtClaims.NAME.value).asString()
                        notification.targetType = NotificationTargetType.user

                        notificationsService.createNotification(notification, configuration)
                    }
                }
                else -> { }
            }


            userProfileUpdate
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

    }

    fun getUserInfo(userType: UserType, userId: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        return when (userType) {
            UserType.student -> getStudentInfoProgram(userId, configuration)
            else -> getOrganisationDepartmentInfoProgram(userId, configuration)
        }
    }

    private fun getOrganisationDepartmentInfoProgram(id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        val userInfo = getUserInfoProgram(id, configuration) as MutableMap

        val mentorsOrganisation =
            when (val orgId = DSL.using(configuration)
                .select()
                .from(organisationUsers)
                .innerJoin(organisations)
                .on(organisationUsers.ORGANISATION_ID.eq(organisations.ID))
                .where(organisationUsers.USER_ID.eq(id))
                .fetchAny()) {
                null -> null
                else -> orgId.map {
                    mapOf(
                        CommonFields.ID.value to it[organisations.ID].toString(),
                        CommonFields.NAME.value to it[organisations.NAME].toString(),
                        CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
                    )
                }
            }


        val mentorsHseDepartment =
            when (val orgId = DSL.using(configuration)
                .select()
                .from(departmentUsers)
                .innerJoin(organisations)
                .on(departmentUsers.ORGANISATION_ID.eq(organisations.ID))
                .where(departmentUsers.USER_ID.eq(id))
                .fetchAny()) {
                null -> null
                else -> orgId.map {
                    mapOf(
                        CommonFields.ID.value to it[organisations.ID].toString(),
                        CommonFields.NAME.value to it[organisations.NAME].toString(),
                        CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
                    )
                }
            }

        val orgOrganisations = when (mentorsOrganisation) {
            null -> emptyList<Map<String, Any>>().toMutableList()
            else -> {
                val ancestors = organisationRelationshipsService.getAncestors(
                    UUID.fromString(mentorsOrganisation?.get(CommonFields.ID.value)),
                    configuration
                )
                ancestors.add(mentorsOrganisation)

                ancestors
            }
        }

        val orgDepartments = when (mentorsHseDepartment) {
            null -> emptyList<Map<String, Any>>().toMutableList()
            else -> {
                val ancestors = organisationRelationshipsService.getAncestors(
                    UUID.fromString(mentorsHseDepartment?.get(CommonFields.ID.value)),
                    configuration
                )
                ancestors.add(mentorsHseDepartment)

                ancestors
            }
        }

        userInfo[MentorFields.ORGANISATION.value] = orgOrganisations
        userInfo[MentorFields.HSE_DEPARTMENT.value] = orgDepartments

        return userInfo
    }


    private fun getStudentInfoProgram(id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        return DSL.using(configuration)
            .select()
            .from(users)
            .innerJoin(studentInfo_students)
            .on(users.ID.eq(studentInfo_students.STUDENT_ID))
            .innerJoin(studentInfo)
            .on(studentInfo_students.STUDENT_INFO_ID.eq(studentInfo.ID))
            .where(users.ID.eq(id))
            .fetchOne()
            .let {
                mapOf(
                    StudentFields.ID.value to it[users.ID]?.toString(),
                    StudentFields.NAME.value to it[users.NAME]?.toString(),
                    StudentFields.ROLE.value to it[users.TYPE]?.toString(),
                    StudentFields.COURSE.value to it[studentInfo.COURSE]?.toString(),
                    StudentFields.FACULTY.value to it[studentInfo.FACULTY]?.toString(),
                    StudentFields.GROUP.value to it[studentInfo.GROUP_NAME]?.toString(),
                    StudentFields.EMAIL.value to it[studentInfo.EMAIL]?.toString(),
                    StudentFields.STATUS.value to convertStudentStatus(it[studentInfo.STATUS]),
                    CommonFields.BLOCKED.value  to convertBlockedStatus(it[users.STATUS])
                )
            }
    }


    private fun getUserInfoProgram(id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        return DSL.using(configuration)
            .select()
            .from(users)
            .where(users.ID.eq(id))
            .fetchOne()
            .let {
                mapOf(
                    UserFields.ID.value to it[users.ID]?.toString(),
                    UserFields.NAME.value to it[users.NAME]?.toString(),
                    UserFields.ROLE.value to it[users.TYPE]?.toString(),
                    UserFields.EMAIL.value to it[users.EMAIL]?.toString(),
                    CommonFields.BLOCKED.value to convertBlockedStatus(it[users.STATUS])
                )
            }
    }

    private fun editDefaultAttributes(id: UUID, body: Any, configuration: Configuration) {
        updateUserAttributes(id, body, configuration)

        val userProfileType = DSL.using(configuration)
            .selectFrom(users)
            .where(users.ID.eq(id))
            .fetchOne()
            .let { it[users.TYPE] }

        when(userProfileType) {
            UserType.student -> updateStudentAttributes(id, body, configuration)
            else -> updateUserOrgDepartmentAttributes(id, body, configuration)
        }
    }

    private fun updateUserAttributes(id: UUID, body: Any, configuration: Configuration) {
        for (key in body.keys() as Set<String>) {
            when(key) {
                UserFields.NAME.value -> updateUserProfileField(id, users.NAME, body[key].toString(), configuration)
                UserFields.EMAIL.value -> updateUserProfileField(id, users.EMAIL, body[key].toString(), configuration)
                UserFields.ROLE.value -> updateUserProfileField(id, users.TYPE, UserType.valueOf(body[key].toString()), configuration)
            }
        }
    }

    private fun updateStudentAttributes(id: UUID, body: Any, configuration: Configuration) {
        for (key in body.keys() as Set<String>) {
            when(key) {
                StudentFields.COURSE.value -> updateStudentInfoField(id, studentInfo.COURSE, body[key].toString().toInt(), configuration)
                StudentFields.FACULTY.value -> updateStudentInfoField(id, studentInfo.FACULTY, convertFaculty(convertFaculty(body[key].toString())), configuration)
                StudentFields.GROUP.value -> updateStudentInfoField(id, studentInfo.GROUP_NAME, body[key].toString(), configuration)
                StudentFields.STATUS.value -> updateStudentInfoField(id, studentInfo.STATUS, convertStudentStatus(body[key].toString()), configuration)
            }
        }
    }

    private fun updateUserOrgDepartmentAttributes(id: UUID, body: Any, configuration: Configuration) {
        for (key in body.keys() as Set<String>) {
            when (key) {
                MentorFields.ORGANISATION.value -> {
                    val orgId = when (body.getOpt(key)?.asOpt<Map<String, kotlin.Any>>()) {
                        null -> null
                        else -> getOptUUID(body[key][CommonFields.ID.value])
                    }

                    updateUserOrganisationField(
                        id,
                        organisationUsers.ORGANISATION_ID,
                        orgId,
                        configuration
                    )
                }
                MentorFields.HSE_DEPARTMENT.value -> {
                    val orgId = when (body.getOpt(key)?.asOpt<Map<String, kotlin.Any>>()) {
                        null -> null
                        else -> getOptUUID(body[key][CommonFields.ID.value])
                    }

                    updateUserDepartmentField(
                        id,
                        departmentUsers.ORGANISATION_ID,
                        orgId,
                        configuration
                    )
                }
            }
        }
    }


    private fun <T> updateUserOrganisationField(id: UUID?, field: TableField<OrganisationUserRecord, T>, value: T, configuration: Configuration) {
        if (value == null) {
            return
        }

        val reprOrg = DSL.using(configuration)
            .selectFrom(organisationUsers)
            .where(organisationUsers.USER_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[organisationUsers.ORGANISATION_ID])
                list
            }

        if (!reprOrg.isEmpty()) {
            DSL.using(configuration)
                .update(organisationUsers)
                .set(field, value)
                .where(organisationUsers.USER_ID.eq(id))
                .execute()
        } else {
            DSL.using(configuration)
                .insertInto(organisationUsers)
                .columns(organisationUsers.USER_ID, organisationUsers.ORGANISATION_ID)
                .values(id, value as UUID)
                .execute()
        }
    }

    private fun <T> updateUserDepartmentField(id: UUID?, field: TableField<DepartmentUserRecord, T>, value: T, configuration: Configuration) {
        if (value == null) {
            return
        }

        val mentorOrg = DSL.using(configuration)
            .selectFrom(departmentUsers)
            .where(departmentUsers.USER_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[departmentUsers.ORGANISATION_ID])
                list
            }

        if (!mentorOrg.isEmpty()) {
            DSL.using(configuration)
                .update(departmentUsers)
                .set(field, value)
                .where(departmentUsers.USER_ID.eq(id))
                .execute()

        } else {
            DSL.using(configuration)
                .insertInto(departmentUsers)
                .columns(departmentUsers.USER_ID, departmentUsers.ORGANISATION_ID)
                .values(id, value as UUID)
                .execute()
        }
    }


    private fun <T> updateUserProfileField(id: UUID, field: TableField<UsersRecord, T>, value: T, configuration: Configuration) {
        DSL.using(configuration)
            .update(users)
            .set(field, value)
            .where(users.ID.eq(id))
            .execute()
    }

    private fun <T> updateStudentInfoField(id: UUID, field: TableField<StudentInfoRecord, T>, value: T, configuration: Configuration) {
        DSL.using(configuration)
            .update(studentInfo)
            .set(field, value)
            .where(studentInfo.ID.eq(getStudentInfoId(id, configuration)))
            .execute()
    }

    private fun getStudentInfoId(id: UUID, configuration: Configuration): UUID {
        return DSL.using(configuration)
            .selectFrom(studentInfo_students)
            .where(studentInfo_students.STUDENT_ID.eq(id))
            .fetchOne()
            .let { it[studentInfo_students.STUDENT_INFO_ID]}
    }

}