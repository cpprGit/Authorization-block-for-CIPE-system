package ru.hse.cppr.service.search

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.ProjectFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.*
import java.util.*
import kotlin.collections.HashMap

class SearchProjectsService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()


    private val projects = Tables.PROJECTS

    private val users = Tables.USERS
    private val activityTable = Tables.ACTIVITY


    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val mentor = params["mentor"]?.first()
        val consultant = params["consultant"]?.first()
        val nameRus = params["projectNameRus"]?.first()
        val nameEng = params["projectNameEng"]?.first()
        val isGroupProject = params["projectIndividuality"]?.first()
        val type = params["projectType"]?.first()
        val activity = params["activity"]?.first()
        val pi = params["pi"]?.first()
        val pmi = params["pmi"]?.first()
        val pad = params["pad"]?.first()

        val projectsM = provider.tx { configuration ->

            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.project, configuration).firstOrNull()
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val projects = using(configuration)
                .select()
                .from(projects)
                .innerJoin(schemaContent)
                .on(projects.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(users)
                .on(projects.LEADER_ID.eq(users.ID))
                .innerJoin(activityTable)
                .on(projects.ACTIVITY_ID.eq(activityTable.ID))
                .where(
                    buildConditionsForProjectSearch(
                        filterParams,
                        mentor,
                        consultant,
                        nameRus,
                        nameEng,
                        isGroupProject,
                        type,
                        activity,
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
                                ProjectFields.ID.value to row[projects.ID].toString(),
                                ProjectFields.PROJECT_NAME_RUS.value to mapOf(
                                    "id" to row[projects.ID]?.toString(),
                                    "name" to row[projects.NAME_RUS]?.toString(),
                                    "type" to "project"
                                ),
                                ProjectFields.PROJECT_NAME_ENG.value to mapOf(
                                    "id" to row[projects.ID]?.toString(),
                                    "name" to row[projects.NAME_ENG]?.toString(),
                                    "type" to "project"
                                ),
                                ProjectFields.PROJECT_TYPE.value to convertProjectType(row[projects.TYPE]),
                                ProjectFields.PROJECT_INDIVIDUALITY.value to convertProjectIndividuality(row[projects.IS_GROUP_PROJECT]),
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT],
                                ProjectFields.MENTOR.value to getUser(
                                    row[projects.LEADER_ID],
                                    configuration
                                ),
                                ProjectFields.CONSULTANT.value to getUser(
                                    row[projects.CONSULTANT_ID],
                                    configuration
                                ),
                                ProjectFields.ACTIVITY.value to getActivity(
                                    row[projects.ACTIVITY_ID].toString(),
                                    configuration
                                ),
                                ProjectFields.PI_COURSES.value to row[projects.PI],
                                ProjectFields.PMI_COURSES.value to row[projects.PMI],
                                ProjectFields.PAD_COURSES.value to row[projects.PAD]
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
        isGroupProject: String?,
        type: String?,
        activity: String?,
        pi: String?,
        pmi: String?,
        pad: String?
    ): org.jooq.Condition? {

        var conditions = users.ID.isNotNull

        if (activity != null) {
            conditions = conditions.and(activityTable.NAME.containsIgnoreCase(activity))
        }

        if (mentor != null) {
            conditions = conditions.and(projects.LEADER_ID.eq(UUID.fromString(mentor)))
        }

        if (consultant != null) {
            conditions = conditions.and(projects.CONSULTANT_ID.eq(UUID.fromString(consultant)))
        }

        if (nameRus != null) {
            conditions = conditions.and(projects.NAME_RUS.containsIgnoreCase(nameRus))
        }

        if (nameEng != null) {
            conditions = conditions.and(projects.NAME_ENG.containsIgnoreCase(nameEng))
        }

        if (isGroupProject != null) {
            conditions = conditions.and(projects.IS_GROUP_PROJECT.equal(convertProjectIndividuality(isGroupProject)))
        }

        if (type != null) {
            conditions = conditions.and(projects.TYPE.equal(convertProjectType(type)))
        }

        if (pi != null) {
            val courses = pi.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projects.PI.containsIgnoreCase(course))
            }
        }

        if (pmi != null) {
            val courses = pmi.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projects.PMI.containsIgnoreCase(course))
            }
        }

        if (pad != null) {
            val courses = pad.toString().trim('[').trim(']').split(',')

            for (course in courses) {
                conditions = conditions.and(projects.PAD.containsIgnoreCase(course))
            }
        }

        return conditions
    }


}