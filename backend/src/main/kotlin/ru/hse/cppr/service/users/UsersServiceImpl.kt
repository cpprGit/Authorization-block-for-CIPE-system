package ru.hse.cppr.service.users

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.any.Any
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.data.database_generated.enums.UserType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.*
import ru.hse.cppr.service.CurrentSchemaService
//import ru.hse.cppr.service.FormattedService.usersTable
import ru.hse.cppr.service.crud.SchemaContentService
import ru.hse.cppr.utils.`as`
import ru.hse.cppr.utils.getOptUUID
import java.sql.Timestamp
import java.util.*

class UsersServiceImpl(override val serviceName: String) : KoinComponent, UsersService {

    private val provider: TxProvider<ForIO>                               by inject()
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val schemaContentService: SchemaContentService                by inject()

    private val usersTable = Tables.USERS
    private val schemaContentTable = Tables.SCHEMA_CONTENT
    private val studentInfoTable = Tables.STUDENT_INFO
    private val studentInfoStudentsTable = Tables.STUDENT_INFO_STUDENTS
    private val organisationUser = Tables.ORGANISATION_USER
    private val departmentUser = Tables.DEPARTMENT_USER

    private val COLUMNS = arrayListOf(
        usersTable.NAME,
        usersTable.EMAIL,
        usersTable.PASSWORD,
        usersTable.TYPE,
        usersTable.SCHEMA_CONTENT_ID,
        usersTable.CREATED_BY,
        usersTable.CREATED_TIME
    )
    private var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, usersTable.ID) }

    private val fetchMapValues = { record: Record ->
        mapOf(
            usersTable.ID.name to record[usersTable.ID]?.toString(),
            usersTable.NAME.name to record[usersTable.NAME]?.toString(),
            usersTable.EMAIL.name to record[usersTable.EMAIL]?.toString(),
            usersTable.PASSWORD.name to record[usersTable.PASSWORD]?.toString(),
            UserFields.ROLE.value to record[usersTable.TYPE]?.toString(),
            usersTable.SCHEMA_CONTENT_ID.name to record[usersTable.SCHEMA_CONTENT_ID]?.toString(),
            usersTable.CREATED_BY.name to record[usersTable.CREATED_BY]?.toString(),
            usersTable.CREATED_TIME.name to record[usersTable.CREATED_TIME]?.toString()
        )
    }

    override fun deleteStudent(id: UUID) {
        val program = provider.tx { configuration ->
            deleteStudentProgram(id, configuration)
        }

        runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }

    override fun deleteUser(id: UUID) {
        val program = provider.tx { configuration ->
            deleteUserProgram(id, configuration)
        }

        runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }


    override fun persistStudent(bodyJson: Any): Map<String, String?> = with (usersTable){
        val program = provider.tx { configuration ->
            createStudentProgram(bodyJson, configuration)
        }

        val savedUser = runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Method: /signup/student POST: Error trying to create user with email=\'${bodyJson["email"].`as`<String>()}\'")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return savedUser
    }

    override fun persistUser(bodyJson: Any): Any = with(usersTable) {
        val program = provider.tx { configuration ->
            createUserProgram(bodyJson, configuration)
        }

        val savedUser = runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Method: /signup POST: Error trying to create user with email=\'${bodyJson["email"].`as`<String>()}\'")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return Any.wrap(savedUser)
    }

    override fun getUser(id: UUID): Map<String, kotlin.Any?> = with(usersTable) {
        val userListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
                .from(usersTable)
                .where(ID.eq(id))
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(
                        fetchMapValues(record)
                    )

                    list
                }
        }


        val user = runBlocking {
            when (val cb = Either.catch { userListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain user.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Form found.")
                    return@runBlocking cb.b
                }
            }
        }

        val res = user.firstOrNull()
        when (res) {
            null -> throw BadRequestException("Bad Id.")
            else -> return res
        }
    }


    private fun createUserProgram(bodyJson: Any, configuration: Configuration): Map<String, String?> {
        val createdStudentProfile = schemaContentService.createProgram(
            Any.wrap(mapOf(
                CommonFields.SCHEMA_ID.value to CurrentSchemaService.getCurrentSchemaProgram(
                    SchemaType.user_profile,
                    configuration
                )
                    .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                CommonFields.SCHEMA_CONTENT.value to bodyJson[CommonFields.SCHEMA_CONTENT.value].toString()
            )),
            configuration
        )

        val userRole = UserType.valueOf(bodyJson[RegistrationFields.ROLE.value].`as`())

        val persistedUser = DSL.using(configuration)
            .insertInto(usersTable)
            .columns(COLUMNS)
            .values(
                bodyJson[RegistrationFields.NAME.value].`as`(),
                bodyJson[RegistrationFields.EMAIL.value].`as`(),
                bodyJson[RegistrationFields.PASSWORD.value].`as`(),
                userRole,
                createdStudentProfile[CommonFields.ID.value].toString().let(UUID::fromString),
                getOptUUID(CommonFields.CREATED_BY.value, bodyJson),
                Timestamp(DateTime.now().millis)
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }

        val organisationId = getOptUUID(MentorFields.ORGANISATION.value, bodyJson)
        val departmentId = getOptUUID(MentorFields.HSE_DEPARTMENT.value, bodyJson)

        if (organisationId != null) {
                    DSL.using(configuration)
                        .insertInto(organisationUser)
                        .columns(organisationUser.ORGANISATION_ID, organisationUser.USER_ID)
                        .values(organisationId, UUID.fromString(persistedUser[CommonFields.ID.value]))
                        .execute()
        }

        if (departmentId != null) {
            DSL.using(configuration)
                .insertInto(departmentUser)
                .columns(departmentUser.ORGANISATION_ID, departmentUser.USER_ID)
                .values(departmentId, UUID.fromString(persistedUser[CommonFields.ID.value]))
                .execute()
        }

        return persistedUser
    }

    private fun deleteUserProgram(id: UUID, configuration: Configuration) {
        val deletedUserProfileId = DSL.using(configuration)
            .deleteFrom(usersTable)
            .where(usersTable.ID.eq(id))
            .returning(usersTable.SCHEMA_CONTENT_ID)
            .fetchOne()
            .map { it[usersTable.SCHEMA_CONTENT_ID]}

        val deleteSchemaContentId =
            DSL.using(configuration)
                .deleteFrom(schemaContentTable)
                .where(schemaContentTable.ID.eq(deletedUserProfileId))
                .execute()
    }





    private fun createStudentProgram(bodyJson: Any, configuration: Configuration): Map<String, String?> {
        val createdStudentProfile = schemaContentService.createProgram(
            Any.wrap(mapOf(
                CommonFields.SCHEMA_ID.value to CurrentSchemaService.getCurrentSchemaProgram(
                    SchemaType.student_profile,
                    configuration
                )
                    .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                CommonFields.SCHEMA_CONTENT.value to bodyJson[CommonFields.SCHEMA_CONTENT.value].toString()
            )),
            configuration
        )
        val studentInfoId = DSL.using(configuration)
            .insertInto(studentInfoTable)
            .columns(
                studentInfoTable.GROUP_NAME,
                studentInfoTable.FACULTY,
                studentInfoTable.COURSE,
                studentInfoTable.EMAIL
            )
            .values(
                bodyJson[StudentFields.GROUP.value].toString(),
                bodyJson[StudentFields.FACULTY.value].`as`(),
                bodyJson[StudentFields.COURSE.value].toString().toInt(),
                bodyJson[StudentFields.EMAIL.value].`as`()
            )
            .returning(studentInfoTable.ID)
            .fetchOne()
            .let {
                it[studentInfoTable.ID]
            }
        val persistedStudent = DSL.using(configuration)
            .insertInto(usersTable)
            .columns(COLUMNS)
            .values(
                bodyJson[RegistrationFields.NAME.value].`as`(),
                bodyJson[RegistrationFields.EMAIL.value].`as`(),
                bodyJson[RegistrationFields.PASSWORD.value].`as`(),
                UserType.student,
                createdStudentProfile[CommonFields.ID.value].toString().let(UUID::fromString),
                getOptUUID(CommonFields.CREATED_BY.value, bodyJson),
                Timestamp(DateTime.now().millis)
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
        val insertIntoStudentInfoStudent = DSL.using(configuration)
            .insertInto(studentInfoStudentsTable)
            .columns(studentInfoStudentsTable.STUDENT_INFO_ID, studentInfoStudentsTable.STUDENT_ID)
            .values(studentInfoId, persistedStudent["id"].toString().let(UUID::fromString))
            .execute()

        return persistedStudent
    }



    private fun deleteStudentProgram(id: UUID, configuration: Configuration) {
        val studentInfoId = DSL.using(configuration)
            .select(studentInfoStudentsTable.STUDENT_INFO_ID)
            .from(studentInfoStudentsTable)
            .where(studentInfoStudentsTable.STUDENT_ID.eq(id))
            .fetchOne()
            .let { it[studentInfoStudentsTable.STUDENT_INFO_ID]}

        val deleteStudentInfoStudentsRecord = DSL.using(configuration)
            .deleteFrom(studentInfoStudentsTable)
            .where(studentInfoStudentsTable.STUDENT_ID.eq(id))
            .execute()

        val deleteStudentInfoRecord = DSL.using(configuration)
            .deleteFrom(studentInfoTable)
            .where(studentInfoTable.ID.eq(studentInfoId))
            .execute()

        val deletedUserProfileId = DSL.using(configuration)
            .deleteFrom(usersTable)
            .where(usersTable.ID.eq(id))
            .returning(usersTable.SCHEMA_CONTENT_ID)
            .fetchOne()
            .map { it[usersTable.SCHEMA_CONTENT_ID]}

        val deleteSchemaContentId =
            DSL.using(configuration)
                .deleteFrom(schemaContentTable)
                .where(schemaContentTable.ID.eq(deletedUserProfileId))
                .execute()
    }



}