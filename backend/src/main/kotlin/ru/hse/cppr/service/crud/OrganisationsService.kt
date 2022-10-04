package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.jooq.*
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.tables.records.OrganisationsRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.OrganisationFormat
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.OrganisationFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.utils.*
import java.util.*

class OrganisationsService(override val serviceName: String) : KoinComponent, CRUDService {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val organisationRelationshipsService: OrganisationRelationshipsService                                by inject()

    val table = Tables.ORGANISATIONS
    val orgaFamilyTable = Tables.ORGANISATION_FAMILY

    val COLUMNS = arrayListOf(
        table.NAME,
        table.IS_HSE_DEPARTMENT,
        table.SCHEMA_CONTENT_ID,
        table.CREATED_BY,
        table.LAST_MODIFIED_BY,
        table.LAST_MODIFIED_TIME,
        table.TYPE,
        table.STATUS
        )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            OrganisationFields.ID.value to record[table.ID]?.toString(),
            OrganisationFields.NAME.value to record[table.NAME]?.toString(),
            OrganisationFields.IS_HSE_DEPARTMENT.value to record[table.IS_HSE_DEPARTMENT],
            CommonFields.SCHEMA_CONTENT_ID.value to record[table.SCHEMA_CONTENT_ID]?.toString(),
            CommonFields.CREATED_BY.value to record[table.CREATED_BY]?.toString(),
            OrganisationFields.LAST_MODIFIED_BY.value to record[table.LAST_MODIFIED_BY]?.toString(),
            OrganisationFields.LAST_MODIFIED_TIME.value to record[table.LAST_MODIFIED_TIME]?.toString(),
            CommonFields.TYPE.value to record[table.TYPE]?.toString(),
            CommonFields.BLOCKED.value to convertBlockedStatus(record[table.BLOCKED_STATUS]),
            OrganisationFields.STATUS.value to record[table.STATUS]?.toString()
            )
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val organisationM = provider.tx { configuration ->
            createOrganisationProgram(
                OrganisationFormat(body),
                body[CommonFields.SCHEMA_CONTENT_ID.value].toString(), configuration
            )
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

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: UUID): Map<String, Any?> {
        val organisationM = provider.tx { configuration ->
            getOrganisationProgram(id, configuration)
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

    private fun updateParent(id: UUID, parentId: UUID?, configuration: Configuration) {
        when (parentId) {
            id, null -> return
            else -> {}
        }

        for (ancestor in organisationRelationshipsService.getAncestors(id, configuration)) {
            if (ancestor[CommonFields.ID.value] == id.toString()) {
                return
            }
        }

        DSL.using(configuration)
            .update(orgaFamilyTable)
            .set(orgaFamilyTable.PARENT_ID, parentId)
            .where(orgaFamilyTable.CHILD_ID.eq(id))
            .execute()

    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        val stageM = provider.tx { configuration ->

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<OrganisationsRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    OrganisationFields.NAME.value ->
                        statement.set(table.NAME, body[key].`as`<String>())
                    OrganisationFields.IS_HSE_DEPARTMENT.value ->
                        statement.set(table.IS_HSE_DEPARTMENT, body[key].`as`<Boolean>())
                    OrganisationFields.PARENT.value -> {
                        val parentId = when (body.getOpt(key)?.asOpt<Map<String, kotlin.Any>>()) {
                            null -> null
                            else -> getOptUUID(body[key][CommonFields.ID.value])
                        }
                        updateParent(id, parentId, configuration)
                        statement.set(table.ID, table.ID)
                    }
                    CommonFields.SCHEMA_CONTENT_ID.value ->
                        statement.set(table.SCHEMA_CONTENT_ID, UUID.fromString(body[key].toString()))
                    else ->
                        statement
                }
            }

            when (updateStatement) {
                is UpdateSetMoreStep ->
                    updateStatement
                        .where(table.ID.eq(id))
                        .returning()
                        .fetchOptional()
                        .map { record ->
                            CommonFields.ID.value to record[table.ID]
                        }

                is UpdateSetFirstStep ->
                    Optional.empty()

                else ->
                    Optional.empty()
            }
        }

        val stage = runBlocking {
            when (val cb = Either.catch { stageM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch organisation.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (stage.isPresent) {
            false -> throw BadRequestException("Organisation with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(stage.get()))
        }
    }

    override fun delete(id: UUID): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun createOrganisationProgram(format: OrganisationFormat, schemaContentId: String, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                format.getName(),
                format.getIsHseDepartment(),
                UUID.fromString(schemaContentId),
                format.getCreatedBy(),
                format.getLastModifiedBy(),
                format.getLastModifiedTime(),
                format.getType(),
                format.getStatus()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let { fetchMapValues(it) }
    }

    fun getOrganisationProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .selectFrom(table)
            .where(table.ID.eq(id))
            .fetchOne()
            .let { fetchMapValues(it) }
    }

}