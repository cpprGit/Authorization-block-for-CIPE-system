package ru.hse.cppr.service.search

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import arrow.optics.extensions.list.cons.cons
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.ProjectRequestStatus
import ru.hse.cppr.data.database_generated.enums.ProjectType
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.convertProjectIndividuality
import ru.hse.cppr.utils.convertProjectRequestStatus
import ru.hse.cppr.utils.convertProjectType
import java.util.*
import kotlin.collections.HashMap

class SearchProjectRequestsService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()


    private val projectRequests = Tables.PROJECT_REQUESTS


    private val users = Tables.USERS

    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val mentor = params["mentor"]?.first()
        val consultant = params["consultant"]?.first()
        val nameRus = params["projectNameRus"]?.first()
        val nameEng = params["projectNameEng"]?.first()
        val status = params["status"]?.first()
        val isGroupProject = params["projectIndividuality"]?.first()
        val type = params["projectType"]?.first()
        val pi = params["pi"]?.first()
        val pmi = params["pmi"]?.first()
        val pad = params["pad"]?.first()

        val projectsM = provider.tx { configuration ->

            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.project_request, configuration).firstOrNull()
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val projects = using(configuration)
                .select()
                .from(projectRequests)
                .innerJoin(schemaContent)
                .on(projectRequests.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(users)
                .on(projectRequests.LEADER_ID.eq(users.ID))
                .where(
                    buildConditionsForProjectSearch(
                        filterParams,
                        mentor,
                        consultant,
                        nameRus,
                        nameEng,
                        status,
                        isGroupProject,
                        type,
                        pi,
                        pmi,
                        pad
                    )
                )
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, row ->
                    if (satisfiesFilterParamSearch(filterParams, row)) {
                        list.add(
                            mapOf(
                                ProjectFields.ID.value to row[projectRequests.ID].toString(),
                                ProjectFields.PROJECT_NAME_RUS.value to mapOf(
                                    CommonFields.ID.value to row[projectRequests.ID].toString(),
                                    CommonFields.NAME.value to row[projectRequests.NAME_RUS].toString(),
                                    CommonFields.TYPE.value to ProfileTypes.PROJECT_REQUEST.value
                                ),
                                ProjectFields.PROJECT_NAME_ENG.value to mapOf(
                                    CommonFields.ID.value to row[projectRequests.ID].toString(),
                                    CommonFields.NAME.value to row[projectRequests.NAME_ENG].toString(),
                                    CommonFields.TYPE.value to ProfileTypes.PROJECT_REQUEST.value
                                ),
                                ProjectFields.PROJECT_TYPE.value to convertProjectType(row[projectRequests.TYPE]),
                                ProjectFields.PROJECT_INDIVIDUALITY.value to convertProjectIndividuality(row[projectRequests.IS_GROUP_PROJECT]),
                                ProjectFields.STATUS.value to convertProjectRequestStatus(row[projectRequests.STATUS]),
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT],
                                ProjectFields.MENTOR.value to getUser(row[projectRequests.LEADER_ID], configuration),
                                ProjectFields.CONSULTANT.value to getUser(
                                    row[projectRequests.CONSULTANT_ID],
                                    configuration
                                ),
                                ProjectFields.PI_COURSES.value to row[projectRequests.PI],
                                ProjectFields.PMI_COURSES.value to row[projectRequests.PMI],
                                ProjectFields.PAD_COURSES.value to row[projectRequests.PAD]
                            )
                        )
                    }
                    list
                }

            val result = HashMap<String, Any>()

            result[CommonFields.SCHEMA.value] = formattedSchema
            result[CommonFields.RECORDS.value] = projects

            result
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




    private fun buildConditionsForProjectSearch(
        filterParams: String?,
        mentor: String?,
        consultant: String?,
        nameRus: String?,
        nameEng: String?,
        status: String?,
        isGroupProject: String?,
        type: String?,
        pi: String?,
        pmi: String?,
        pad: String?
    ): org.jooq.Condition? {

        var conditions = users.ID.isNotNull

        if (mentor != null) {
            conditions = conditions.and(projectRequests.LEADER_ID.eq(UUID.fromString(mentor)))
        }

        if (consultant != null) {
            conditions = conditions.and(projectRequests.CONSULTANT_ID.eq(UUID.fromString(consultant)))
        }

        if (nameRus != null) {
            conditions = conditions.and(projectRequests.NAME_RUS.containsIgnoreCase(nameRus))
        }

        if (nameEng != null) {
            conditions = conditions.and(projectRequests.NAME_ENG.containsIgnoreCase(nameEng))
        }

        if (status != null) {
            conditions = conditions.and(projectRequests.STATUS.equal(convertProjectRequestStatus(status)))
        }

        if (isGroupProject != null) {
            conditions = conditions.and(projectRequests.IS_GROUP_PROJECT.equal(convertProjectIndividuality(isGroupProject)))
        }

        if (type != null) {
            conditions = conditions.and(projectRequests.TYPE.equal(convertProjectType(type)))
        }

        if (pi != null) {
            val courses = pi.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projectRequests.PI.containsIgnoreCase(course))
            }
        }

        if (pmi != null) {
            val courses = pmi.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projectRequests.PMI.containsIgnoreCase(course))
            }
        }

        if (pad != null) {
            val courses = pad.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projectRequests.PAD.containsIgnoreCase(course))
            }
        }



        return conditions
    }

    private fun getUser(id: UUID?, configuration: Configuration): Map<String, Any> {
        if (id == null) {
            return mapOf(
                CommonFields.ID.value to "",
                CommonFields.NAME.value to ""
            )
        }

        return DSL.using(configuration)
            .selectFrom(users)
            .where(users.ID.eq(id))
            .fetchOne()
            .map { record ->
                mapOf(
                    CommonFields.ID.value to record[users.ID].toString(),
                    CommonFields.NAME.value to record[users.NAME].toString(),
                    CommonFields.TYPE.value to ProfileTypes.USER.value
                )
            }
    }

}