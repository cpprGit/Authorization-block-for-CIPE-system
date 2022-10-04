package ru.hse.cppr.service.search

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.JsonIterator
import kotlinx.coroutines.runBlocking
import org.jooq.Record
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.data.database_generated.enums.StudentStatus
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.StudentFields
import ru.hse.cppr.representation.enums.fields.UserFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.convertBlockedStatus
import ru.hse.cppr.utils.convertStudentStatus
import java.util.*
import kotlin.collections.HashMap

class SearchStudentsService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()

    private val users = Tables.USERS
    private val studentInfo = Tables.STUDENT_INFO
    private val studentInfoStudents = Tables.STUDENT_INFO_STUDENTS


    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val course = params["course"]?.first()
        val name = params["name"]?.first()
        val faculty = params["faculty"]?.first()
        val group = params["group"]?.first()
        val email = params["email"]?.first()
        val status = params["status"]?.first()
        val blocked = params["blocked"]?.first()

        val userM = provider.tx { configuration ->
            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.student_profile, configuration).firstOrNull()
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val students = using(configuration)
                .select()
                .from(users)
                .innerJoin(schemaContent)
                .on(users.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(studentInfoStudents)
                .on(users.ID.eq(studentInfoStudents.STUDENT_ID))
                .innerJoin(studentInfo)
                .on(studentInfoStudents.STUDENT_INFO_ID.eq(studentInfo.ID))
                .where(buildConditionsForStudentSearch(filterParams, course, name, faculty, group, email, status, blocked))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, row ->
                    if (satisfiesFilterParamSearch(filterParams, row)) {
                        list.add(
                            mapOf(
                                StudentFields.ID.value to row[users.ID].toString(),
                                StudentFields.NAME.value to mapOf(
                                    CommonFields.ID.value to row[users.ID].toString(),
                                    CommonFields.NAME.value to row[users.NAME],
                                    CommonFields.TYPE.value to ProfileTypes.USER.value
                                ),
                                StudentFields.ROLE.value to row[users.TYPE],
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT],
                                CommonFields.BLOCKED.value to convertBlockedStatus(row[users.STATUS]),
                                StudentFields.COURSE.value to row[studentInfo.COURSE],
                                StudentFields.FACULTY.value to row[studentInfo.FACULTY],
                                StudentFields.GROUP.value to row[studentInfo.GROUP_NAME],
                                StudentFields.EMAIL.value to row[studentInfo.EMAIL],
                                StudentFields.STATUS.value to convertStudentStatus(row[studentInfo.STATUS])
                            )
                        )
                    }

                    list
                }


            (formattedSchema[CommonFields.FIELDS.value] as MutableList<Map<String, Any?>>).removeIf { field ->
                val attr = field[CommonFields.ATTRIBUTE.value] as Map<String, Any?>

                (attr[CommonFields.USAGE.value].toString().toLowerCase() == CommonFields.PASSWORD.value) ||
                        (attr[CommonFields.NAME.value].toString() == "User Role")
            }

            val result = HashMap<String, Any>()

            result[CommonFields.SCHEMA.value] = formattedSchema
            result[CommonFields.RECORDS.value] = students

            result
        }

        return runBlocking {
            when (val cb = Either.catch { userM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }

    private fun buildConditionsForStudentSearch(
        filterParams: String?,
        course: String?,
        name: String?,
        faculty: String?,
        group: String?,
        email: String?,
        status: String?,
        blocked: String?
    ): org.jooq.Condition? {

        var conditions = users.ID.isNotNull

        if (course != null) {
            conditions = conditions.and(studentInfo.COURSE.equal(course.toInt()))
        }

        if (name != null) {
            conditions = conditions.and(users.NAME.containsIgnoreCase(name))
        }

        if (faculty != null) {
            conditions = conditions.and(studentInfo.FACULTY.containsIgnoreCase(faculty))
        }

        if (group != null) {
            conditions = conditions.and(studentInfo.GROUP_NAME.containsIgnoreCase(group))
        }

        if (email != null) {
            conditions = conditions.and(studentInfo.EMAIL.containsIgnoreCase(email))
        }

        if (status != null) {
            conditions = conditions.and(studentInfo.STATUS.equal(convertStudentStatus(status)))
        }


        if (blocked != null) {
            conditions = conditions.and(users.STATUS.eq(convertBlockedStatus(blocked.toBoolean())))
        }



        return conditions
    }
}