package ru.hse.cppr.service.search

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.JsonIterator
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.MentorFields
import ru.hse.cppr.representation.enums.fields.UserFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.convertBlockedStatus
import ru.hse.cppr.utils.convertStudentStatus
import ru.hse.cppr.utils.convertUserRole
import java.util.*
import kotlin.collections.HashMap

class SearchUsersService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()

    private val users = Tables.USERS
    private val organisationUser = Tables.ORGANISATION_USER
    private val departmentUser = Tables.DEPARTMENT_USER
    private val organisationsTable = Tables.ORGANISATIONS


    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val role = params["role"]?.first()
        val filterParams = params["filter_params"]?.first()
        val organisation = params["organisation"]?.first()
        val department = params["hseDepartment"]?.first()
        val name = params["name"]?.first()
        val email = params["email"]?.first()
        val blocked = params["blocked"]?.first()

        val userM = provider.tx { configuration ->
            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.user_profile, configuration).firstOrNull()
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val users = using(configuration)
                .select()
                .from(users)
                .innerJoin(schemaContent)
                .on(users.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .leftOuterJoin(organisationUser)
                .on(users.ID.eq(organisationUser.USER_ID))
                .leftOuterJoin(departmentUser)
                .on(users.ID.eq(departmentUser.USER_ID))
                .where(buildConditionsForStudentSearch(filterParams, role, name, organisation, department, email, blocked))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, row ->

                    if (satisfiesFilterParamSearch(filterParams, row)) {
                        list.add(
                            mapOf(
                                UserFields.ID.value to row[users.ID].toString(),
                                UserFields.NAME.value to mapOf(
                                    CommonFields.ID.value to row[users.ID].toString(),
                                    CommonFields.NAME.value to row[users.NAME],
                                    CommonFields.TYPE.value to ProfileTypes.USER.value
                                ),
                                UserFields.EMAIL.value to row[users.EMAIL],
                                UserFields.ROLE.value to row[users.TYPE],
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT],
                                CommonFields.BLOCKED.value to convertBlockedStatus(row[users.STATUS]),
                                MentorFields.ORGANISATION.value to getOrganisation(row, configuration),
                                MentorFields.HSE_DEPARTMENT.value to getDepartment(row, configuration)
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
            result[CommonFields.RECORDS.value] = users

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

    private fun getOrganisation(record: Record, configuration: Configuration): Any? {
        if (record[organisationUser.ORGANISATION_ID] != null) {
            return mapOf(
                CommonFields.ID.value to record[organisationUser.ORGANISATION_ID]?.toString(),
                CommonFields.NAME.value to getOrgName(record[organisationUser.ORGANISATION_ID], configuration),
                CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
            )
        }
        return null
    }

    private fun getDepartment(record: Record, configuration: Configuration): Any? {
        if (record[departmentUser.ORGANISATION_ID] != null) {
            return mapOf(
                CommonFields.ID.value to record[departmentUser.ORGANISATION_ID]?.toString(),
                CommonFields.NAME.value to getOrgName(record[departmentUser.ORGANISATION_ID], configuration),
                CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
            )
        }
        return null
    }

    private fun getOrgName(id: UUID, configuration: Configuration): String {
        return DSL.using(configuration)
            .selectFrom(organisationsTable)
            .where(organisationsTable.ID.eq(id))
            .fetchOne()
            .map { it[organisationsTable.NAME].toString() }
    }


    private fun buildConditionsForStudentSearch(
        filterParams: String?,
        role: String?,
        name: String?,
        organisation: String?,
        department: String?,
        email: String?,
        blocked: String?
    ): org.jooq.Condition? {

        var conditions = users.ID.isNotNull

        if (role != null) {
            conditions = conditions.and(users.TYPE.eq(convertUserRole(role)))
        }

        if (organisation != null) {
            conditions = conditions.and(organisationUser.ORGANISATION_ID.eq(UUID.fromString(organisation)))
        }

        if (department != null) {
            conditions = conditions.and(departmentUser.ORGANISATION_ID.eq(UUID.fromString(department)))
        }

        if (name != null) {
            conditions = conditions.and(users.NAME.containsIgnoreCase(name))
        }

        if (email != null) {
            conditions = conditions.and(users.EMAIL.containsIgnoreCase(email))
        }


        if (blocked != null) {
            conditions = conditions.and(users.STATUS.eq(convertBlockedStatus(blocked.toBoolean())))
        }


        return conditions
    }
}