package ru.hse.cppr.service.studentgroups

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
import java.util.*

class StudentGroupsServiceImpl(override val serviceName: String) : KoinComponent, StudentGroupsService {
    private val provider: TxProvider<ForIO>                               by inject()

    private val variantsTable = Tables.VARIANTS
    private val attributeVariantsTable = Tables.ATTRIBUTE_VARIANTS


    override fun updateStudentGroupLists(body: com.jsoniter.any.Any) {
        val txResult = provider.tx { configuration ->
            val groups = getStudentGroupsProgram(configuration)

            for (group in groups) {
                deleteStudentGroup(group, configuration)
            }

            for (group in body) {
                addStudentGroup(group.toString(), configuration)
            }
        }

        runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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


    override fun deleteStudentGroup(groupName: String, configuration: Configuration) {
        val groupAttributeId = UUID.fromString("00000000-0000-0000-0000-03b7f3a00003")

        val variantId =
                DSL.using(configuration)
                        .selectFrom(variantsTable)
                        .where(variantsTable.VARIANT.eq(groupName))
                        .fetchOne()
                        .map { it[variantsTable.ID] }

        DSL.using(configuration)
                .deleteFrom(attributeVariantsTable)
                .where(attributeVariantsTable.VARIANT_ID.eq(variantId))
                .execute()

        DSL.using(configuration)
                .deleteFrom(variantsTable)
                .where(variantsTable.ID.eq(variantId))
                .execute()

    }


    override fun addStudentGroup(groupName: String, configuration: Configuration) {
        val groupAttributeId = UUID.fromString("00000000-0000-0000-0000-03b7f3a00003")

        val newVariantId = DSL.using(configuration)
                .insertInto(variantsTable)
                .columns(
                        variantsTable.VARIANT
                )
                .values(groupName)
                .returning()
                .fetchOne()
                .map { it[variantsTable.ID]}

        DSL.using(configuration)
                .insertInto(attributeVariantsTable)
                .columns(attributeVariantsTable.VARIANT_ID, attributeVariantsTable.ATTRIBUTE_ID)
                .values(newVariantId, groupAttributeId)
                .execute()
    }


    override fun getStudentGroupsProgram(configuration: Configuration): List<String> {
        val groupAttributeId = UUID.fromString("00000000-0000-0000-0000-03b7f3a00003")

        return DSL.using(configuration)
                .select()
                .from(attributeVariantsTable)
                .innerJoin(variantsTable)
                .on(attributeVariantsTable.VARIANT_ID.eq(variantsTable.ID))
                .where(attributeVariantsTable.ATTRIBUTE_ID.eq(groupAttributeId))
                .fetch()
                .fold(mutableListOf<String>()) { list, record ->
                    list.add(record[variantsTable.VARIANT])
                    list
                }
    }

    override fun getStudentGroups(): List<String> {
        val txResult = provider.tx { configuration ->
            getStudentGroupsProgram(configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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
}