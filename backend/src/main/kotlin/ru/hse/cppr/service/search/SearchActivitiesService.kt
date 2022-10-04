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
import ru.hse.cppr.data.database_generated.enums.ActivityStatus
import ru.hse.cppr.data.database_generated.enums.FacultyType
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.ActivityFields
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.convertActivityStatus
import ru.hse.cppr.utils.convertFaculty
import java.util.*
import kotlin.collections.HashMap

class SearchActivitiesService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()

    private val activities = Tables.ACTIVITY

    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val course = params["course"]?.first()
        val faculty = params["faculty"]?.first()
        val name = params["name"]?.first()
        val year = params["year"]?.first()
        val status = params["status"]?.first()
        val description = params["description"]?.first()

        val userM = provider.tx { configuration ->
            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.activity, configuration).firstOrNull()

            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val activities = using(configuration)
                .select()
                .from(activities)
                .innerJoin(schemaContent)
                .on(schemaContent.ID.eq(activities.SCHEMA_CONTENT_ID))
                .where(buildConditionsForActivitySearch(filterParams, course, faculty, year, name, status, description))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                    if (satisfiesFilterParamSearch(filterParams, record)) {
                        list.add(
                            mapOf(
                                ActivityFields.ID.value to record[activities.ID]?.toString(),
                                ActivityFields.NAME.value to mapOf(
                                    CommonFields.ID.value to record[activities.ID].toString(),
                                    CommonFields.NAME.value to record[activities.NAME].toString(),
                                    CommonFields.TYPE.value to ProfileTypes.ACTIVITY.value
                                ),
                                ActivityFields.DESCRIPTION.value to record[activities.DESCRIPTION]?.toString(),
                                ActivityFields.FACULTY.value to convertFaculty(record[activities.FACULTY]),
                                ActivityFields.YEAR.value to record[activities.YEAR]?.toString(),
                                ActivityFields.COURSE.value to record[activities.COURSE]?.toString(),
                                ActivityFields.STATUS.value to convertActivityStatus(record[activities.STATUS])
                            )
                        )
                    }

                    list
                }

            val result = HashMap<String, Any>()

            (formattedSchema[CommonFields.FIELDS.value] as MutableList<Map<String, Any?>>).removeIf { field ->
                field[CommonFields.NAME.value].toString() == "stages"
            }

            result[CommonFields.SCHEMA.value] = formattedSchema
            result[CommonFields.RECORDS.value] = activities

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


    private fun buildConditionsForActivitySearch(
        filterParams: String?,
        course: String?,
        faculty: String?,
        year: String?,
        name: String?,
        status: String?,
        description: String?
    ): org.jooq.Condition? {

        var conditions = activities.ID.isNotNull

        if (course != null) {
            conditions = conditions.and(activities.COURSE.equal(course.toInt()))
        }

        if (faculty != null) {
            conditions = conditions.and(activities.FACULTY.equal(convertFaculty(faculty)))
        }

        if (year != null) {
            conditions = conditions.and(activities.YEAR.eq(year.toInt()))
        }

        if (name != null) {
            conditions = conditions.and(activities.NAME.containsIgnoreCase(name))
        }

        if (description != null) {
            conditions = conditions.and(activities.DESCRIPTION.containsIgnoreCase(description))
        }

        if (status != null) {
            conditions = conditions.and(activities.STATUS.equal(convertActivityStatus(status)))
        }


        conditions = conditions.and(activities.ID.notEqual(UUID.fromString("00000000-0000-0000-0000-000000000000")))


        return conditions
    }
}