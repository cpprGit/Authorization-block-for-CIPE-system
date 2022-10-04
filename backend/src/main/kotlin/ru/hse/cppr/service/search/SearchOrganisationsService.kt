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
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.OrganisationFields
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.utils.convertBlockedStatus
import java.util.*
import kotlin.collections.HashMap

class SearchOrganisationsService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val provider: TxProvider<ForIO>                                   by inject()

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val organisationRelationshipsService: OrganisationRelationshipsService      by inject()


    private val organisations = Tables.ORGANISATIONS
    private val orgFamilyTable = Tables.ORGANISATION_FAMILY


    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val name = params["orgName"]?.first()
        val hseDepartment = params["hseDepartment"]?.first()
        val parent = params["parent"]?.first()
        val blocked = params["blocked"]?.first()


        val organisationsM = provider.tx { configuration ->
            val schema =
                CurrentSchemaService.getCurrentSchemaProgram(SchemaType.org_profile, configuration).firstOrNull()
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schema?.get(CommonFields.SCHEMA_ID.value).toString())
            )

            val organisations = using(configuration)
                .select()
                .from(organisations)
                .innerJoin(schemaContent)
                .on(organisations.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(orgFamilyTable)
                .on(orgFamilyTable.CHILD_ID.eq(organisations.ID))
                .where(
                    buildConditionsForProjectSearch(
                        filterParams,
                        name,
                        hseDepartment,
                        parent,
                        blocked
                    )
                )
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, row ->
                    if (satisfiesFilterParamSearch(filterParams, row)) {
                        list.add(
                            mapOf(
                                OrganisationFields.ID.value to row[organisations.ID].toString(),
                                OrganisationFields.NAME.value to mapOf(
                                    CommonFields.ID.value to row[organisations.ID]?.toString(),
                                    CommonFields.NAME.value to row[organisations.NAME]?.toString(),
                                    CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
                                ),
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT],
                                OrganisationFields.IS_HSE_DEPARTMENT.value to row[organisations.IS_HSE_DEPARTMENT],
                                CommonFields.BLOCKED.value to convertBlockedStatus(row[organisations.BLOCKED_STATUS]),
                                OrganisationFields.PARENT.value to organisationRelationshipsService.getAncestors(
                                    row[organisations.ID],
                                    configuration
                                )
                            )
                        )
                    }
                    list
                }

            val result = HashMap<String, Any>()

            result[CommonFields.SCHEMA.value] = formattedSchema
            result[CommonFields.RECORDS.value] = organisations

            result
        }

        return runBlocking {
            when (val cb = Either.catch { organisationsM.fix().unsafeRunSync() }) {
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
        name: String?,
        hseDepartment: String?,
        parent: String?,
        blocked: String?
    ): org.jooq.Condition? {

        var conditions = organisations.ID.isNotNull

        if (name != null) {
            conditions = conditions.and(organisations.NAME.containsIgnoreCase(name))
        }

        if (hseDepartment != null) {
            conditions = conditions.and(organisations.IS_HSE_DEPARTMENT.eq(hseDepartment.toBoolean()))
        }

        if (parent != null) {
            conditions = conditions.and(orgFamilyTable.PARENT_ID.eq(UUID.fromString(parent)))
        }


        if (blocked != null) {
            conditions = conditions.and(organisations.BLOCKED_STATUS.eq(convertBlockedStatus(blocked.toBoolean())))
        }

        return conditions
    }


}