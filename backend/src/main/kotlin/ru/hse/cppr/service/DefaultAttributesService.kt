package ru.hse.cppr.service

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
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.service.crud.AttributesDictionaryService
import ru.hse.cppr.utils.getFields
import java.util.*

object DefaultAttributesService : KoinComponent {

    private val provider: TxProvider<ForIO>                                         by inject()
    private val attributesDictionaryService: AttributesDictionaryService            by inject()

    private val defaultAttributes = Tables.DEFAULT_ATTRIBUTES
    private val attributesTable = Tables.ATTRIBUTES

    fun getDefaultFieldsForSchemaType(schemaType: String): MutableList<Map<String, Any>> {
        val attrsM = provider.tx { configuration ->
            getFields(configuration, SchemaType.valueOf(schemaType))
        }


        val result = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return result
    }



    fun getDefaultAttributesForSchemaType(schemaType: String): ArrayList<MutableMap<String, Any>> {
        val attrsM = provider.tx { configuration ->
            getDefaultAttributesForSchemaTypeProgram(schemaType, configuration)
        }


        val result = runBlocking {
            when (val cb = Either.catch { attrsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return result
    }


    fun getDefaultAttributesForSchemaTypeProgram(
        schemaType: String,
        configuration: Configuration
    ): ArrayList<MutableMap<String, Any>> {

        val attrIds = DSL.using(configuration)
            .select(defaultAttributes.ATTRIBUTE_ID)
            .from(defaultAttributes)
            .innerJoin(attributesTable)
            .on(defaultAttributes.ATTRIBUTE_ID.eq(attributesTable.ID))
            .where(defaultAttributes.SCHEMA_TYPE.eq(SchemaType.valueOf(schemaType)))
            .orderBy(attributesTable.ORDERING.asc())
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[defaultAttributes.ATTRIBUTE_ID])

                list
            }

        val attributes = ArrayList<MutableMap<String, Any>>()

        for (attrId in attrIds) {
            attributes.add(attributesDictionaryService.getAttributeProgram(configuration, attrId))
        }

        return attributes
    }
}