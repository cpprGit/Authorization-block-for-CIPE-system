package ru.hse.cppr.service.organisation

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.formats.ProfileTypes
import java.util.*

class OrganisationRelationshipsServiceImpl(override val serviceName: String) : OrganisationRelationshipsService, KoinComponent {
    val provider: TxProvider<ForIO>                                                 by inject()

    private val organisations = Tables.ORGANISATIONS
    private val organisationFamily = Tables.ORGANISATION_FAMILY

    override fun getAncestors(id: UUID): MutableList<Map<String, Any>> {
        val programM = provider.tx { configuration ->
            getAncestors(id, mutableListOf(), configuration).asReversed()
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun getAncestors(id: UUID?, configuration: Configuration): MutableList<Map<String, Any>> {
        if (id == null) {
            return emptyList<Map<String, Any>>().toMutableList()
        }
        return getAncestors(id, mutableListOf(), configuration).asReversed()
    }


    override fun getDescendants(id: UUID?): MutableList<Map<String, Any>> {
        val programM = provider.tx { configuration ->

            val predicate = when (id) {
                null -> organisationFamily.PARENT_ID.isNull
                else -> organisationFamily.PARENT_ID.eq(id)
            }

            DSL.using(configuration)
                .select()
                .from(organisationFamily)
                .innerJoin(organisations)
                .on(organisations.ID.eq(organisationFamily.CHILD_ID))
                .where(predicate)
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any>>()) { list, record ->
                    list.add(mapOf(
                        CommonFields.ID.value to record[organisations.ID].toString(),
                        CommonFields.NAME.value to record[organisations.NAME],
                        CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value,
                        "hasChildren" to when (
                                DSL.using(configuration)
                                    .selectFrom(organisationFamily)
                                    .where(organisationFamily.PARENT_ID.eq(record[organisations.ID]))
                                    .fetchAny())
                        {
                            null -> false
                            else -> true
                        }
                    ))

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }




    override fun addRelation(parentId: UUID, childId: UUID, configuration: Configuration): Map<String, String> {
        val programM = provider.tx { configuration ->

            DSL.using(configuration)
                .deleteFrom(organisationFamily)
                .where(
                    organisationFamily.CHILD_ID.eq(childId)
                        .and(organisationFamily.PARENT_ID.isNull)
                )
                .execute()

            DSL.using(configuration)
                .insertInto(organisationFamily)
                .columns(organisationFamily.PARENT_ID, organisationFamily.CHILD_ID)
                .values(parentId, childId)
                .execute()

            mapOf(
                "status" to "success"
            )
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)

                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }



    private fun getParent(id: UUID?, configuration: Configuration): Pair<String, UUID>? {
        return DSL.using(configuration)
            .select()
            .from(organisationFamily)
            .innerJoin(organisations)
            .on(organisations.ID.eq(organisationFamily.PARENT_ID))
            .where(organisationFamily.CHILD_ID.eq(id))
            .fetch()
            .fold (mutableListOf<Pair<String, UUID>>()) { list, record ->
                list.add(
                    Pair<String, UUID>(record[organisations.NAME].toString(), record[organisationFamily.PARENT_ID])
                )
                list
            }
            .firstOrNull()
    }


    //Is recursive function
    private fun getAncestors(id: UUID?, ancestors: MutableList<Map<String, kotlin.Any>>, configuration: Configuration): MutableList<Map<String, kotlin.Any>> {
        if (id == null) {
            return emptyList<Map<String, Any>>().toMutableList()
        }

        val parent = getParent(id, configuration)

        return when (parent) {
            null -> ancestors
            else -> {
                ancestors.add(
                    mapOf(
                        CommonFields.ID.value to parent.second.toString(),
                        CommonFields.NAME.value to parent.first,
                        CommonFields.TYPE.value to ProfileTypes.ORGANISATION.value
                    )
                )
                getAncestors(parent.second, ancestors, configuration)
            }
        }
    }


}