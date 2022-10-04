package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
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
import ru.hse.cppr.representation.formats.OrganisationFormat
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.OrganisationFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.crud.*
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.service.posts.PostsService
import java.util.*

class FormattedOrganisationsService(override val serviceName: String): KoinComponent, CRUDService {
    val provider: TxProvider<ForIO>                                     by inject()
    val log: Log                                                        by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val schemaContentService: SchemaContentService                              by inject()
    private val organisationsService: OrganisationsService                              by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService            by inject()
    private val postsService: PostsService                                              by inject()

    private val organisationRelationshipsService: OrganisationRelationshipsService      by inject()

    private val organisationTable = Tables.ORGANISATIONS
    private val organisationUsersTable = Tables.ORGANISATION_USER
    private val organisationFamily = Tables.ORGANISATION_FAMILY
    private val users = Tables.USERS
    private val schemaContent = Tables.SCHEMA_CONTENT


    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
       val organisationM = provider.tx { configuration ->
           createOrganisationProgram(body, configuration)
       }

        return runBlocking {
            when (val cb = Either.catch { organisationM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    fun createOrganisationProgram(body: com.jsoniter.any.Any, configuration: Configuration): Map<String, Any?> {
        val createdOrganisationProfile = schemaContentService.createProgram(
            com.jsoniter.any.Any.wrap(
                mapOf(
                    CommonFields.SCHEMA_ID.value to CurrentSchemaService
                        .getCurrentSchemaProgram(SchemaType.org_profile, configuration)
                        .firstOrNull()?.get(CommonFields.SCHEMA_ID.value).toString(),
                    CommonFields.SCHEMA_CONTENT.value to body[CommonFields.SCHEMA_CONTENT.value].toString()
                )
            ),
            configuration
        )

        val formattedBody =  OrganisationFormat(body)

        val org = organisationsService.createOrganisationProgram(
            formattedBody,
            createdOrganisationProfile[CommonFields.ID.value].toString(), configuration
        )

        when (val parentId = formattedBody.getParent()) {

            null ->
                DSL.using(configuration)
                    .insertInto(organisationFamily)
                    .columns(organisationFamily.CHILD_ID, organisationFamily.PARENT_ID)
                    .values(org[CommonFields.ID.value].toString().let(UUID::fromString), null)
                    .execute()

            else -> organisationRelationshipsService.addRelation(
                parentId,
                org[CommonFields.ID.value].toString().let(UUID::fromString),
                configuration
            )

        }


        return org
    }

    override fun get(id: UUID): Map<String, Any?> {
        val profileM = provider.tx { configuration ->

            val organisation = organisationsService.getOrganisationProgram(id, configuration)

            val formattedSchema =
                formattedSchemaContentService.getFormattedSchemaContentProgram(
                    configuration,
                    UUID.fromString(organisation[CommonFields.SCHEMA_CONTENT_ID.value].toString())
                )
                    .toMutableMap()

            for (key in organisation.keys) {
                when (key) {
                    OrganisationFields.LAST_MODIFIED_BY.value -> formattedSchema[OrganisationFields.LAST_MODIFIED_BY.value] =
                        getUser(UUID.fromString(organisation[key].toString()), configuration)
                    CommonFields.CREATED_BY.value -> formattedSchema[CommonFields.CREATED_BY.value] =
                        getUser(UUID.fromString(organisation[key].toString()), configuration)
                    else -> formattedSchema[key] = organisation[key]
                }
            }

            formattedSchema[OrganisationFields.PARENT.value] = organisationRelationshipsService.getAncestors(id, configuration)

            formattedSchema[OrganisationFields.REPRESENTATIVES.value] =
                getOrganisationRepresentatives(id, configuration)
            formattedSchema[OrganisationFields.MENTORS.value] = getOrganisationMentors(id, configuration)
            formattedSchema[CommonFields.POSTS.value] = postsService.getPostsByProfileIdProgram(
                UUID.fromString(organisation[CommonFields.SCHEMA_CONTENT_ID.value].toString()),
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


    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        val txResult = provider.tx { configuration ->

            val orgProfileId = DSL.using(configuration)
                .select(organisationTable.SCHEMA_CONTENT_ID)
                .from(organisationTable)
                .where(organisationTable.ID.eq(id))
                .fetchOne()
                .let { it[organisationTable.SCHEMA_CONTENT_ID] }

            val orgProfileUpdate = DSL.using(configuration)
                .update(schemaContent)
                .set(schemaContent.CONTENT, body[CommonFields.SCHEMA_CONTENT.value].toString())
                .where(schemaContent.ID.eq(orgProfileId))
                .execute()
        }

        runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

       return organisationsService.update(id, body)
    }

    override fun delete(id: UUID): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getUser(id: UUID?, configuration: Configuration): Map<String, Any> {
        if (id == null) {
            return mapOf(
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


    private fun getOrganisationRepresentatives(id: UUID, configuration: Configuration): MutableList<Map<String, kotlin.Any>> {
        return getOrganisationUsers(id, UserType.representative, configuration)
    }

    private fun getOrganisationMentors(id: UUID, configuration: Configuration): MutableList<Map<String, kotlin.Any>> {
        return getOrganisationUsers(id, UserType.mentor, configuration)
    }

    private fun getOrganisationUsers(id: UUID, userType: UserType, configuration: Configuration): MutableList<Map<String, kotlin.Any>> {
        return DSL.using(configuration)
            .select()
            .from(organisationUsersTable)
            .innerJoin(users)
            .on(organisationUsersTable.USER_ID.eq(users.ID))
            .where(
                organisationUsersTable.USER_ID.eq(id)
                    .and(users.TYPE.eq(userType))
            )
            .fetch()
            .fold(mutableListOf()) { list, record ->
                list.add(
                    mapOf(
                        CommonFields.ID.value to record[organisationUsersTable.USER_ID].toString(),
                        CommonFields.NAME.value to record[users.NAME],
                        CommonFields.TYPE.value to ProfileTypes.USER.value
                    )
                )
                list
            }
    }

}