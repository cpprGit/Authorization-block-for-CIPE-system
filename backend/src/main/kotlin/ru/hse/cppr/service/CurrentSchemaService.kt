package ru.hse.cppr.service

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.any.Any
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
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.crud.SchemaService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.getOptUUID
import java.util.*
import kotlin.streams.toList

object CurrentSchemaService: KoinComponent {
    private val provider: TxProvider<ForIO>                                   by inject()
    private val log: Log                                                      by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val schemaService: SchemaService                                  by inject()

    private val table = Tables.CURRENT_SCHEMAS
    private val schemaContentTable = Tables.SCHEMA_CONTENT
    private val schemaAttributesTable = Tables.SCHEMA_ATTRIBUTES
    private val schemasDictionaryTable = Tables.SCHEMAS_DICTIONARY

    private val COLUMNS = arrayListOf(table.SCHEMA_ID, table.TYPE)
    private var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    private val fetchMapValues = { record: Record ->
        mapOf(
            CommonFields.ID.value to record[table.ID].toString(),
            CommonFields.SCHEMA_ID.value to record[table.SCHEMA_ID].toString(),
            table.TYPE.name to record[table.TYPE].toString()
        )
    }


    fun getFormatted(type: SchemaType): Map<String, kotlin.Any?> {
        return formattedSchemaService.get(UUID.fromString(get(type)[CommonFields.SCHEMA_ID.value].toString()))
    }


    fun get(type: SchemaType): Map<String, kotlin.Any?> = with (table) {
        val formListM = provider.tx { configuration ->
           getCurrentSchemaProgram(type, configuration)
        }


        val form = runBlocking {
            when (val cb = Either.catch { formListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to obtain form.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Form found.")
                    return@runBlocking cb.b
                }
            }
        }

        val res = form.firstOrNull()
        when(res) {
            null -> throw BadRequestException("Bad Type.")
            else -> return res
        }
    }


    fun duplicateSchemaWithNewType(id: UUID, schemaType: SchemaType, configuration: Configuration): UUID {
        val schemaAttributeIds = DSL.using(configuration)
            .selectFrom(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[schemaAttributesTable.ATTRIBUTE_ID])

                list
            }

        val schema = DSL.using(configuration)
            .selectFrom(schemasDictionaryTable)
            .where(schemasDictionaryTable.ID.eq(id))
            .fetchOne()
            .map { record ->
                schemaService.fetchMapValues(record)
            }


        val newSchemaId = DSL.using(configuration)
            .insertInto(schemasDictionaryTable)
            .columns(schemaService.COLUMNS)
            .values(
                schema["title"].toString() + UUID.randomUUID().toString(),
                schema["description"],
                schemaType,
                getOptUUID(schema["createdBy"]),
                schema["buttonName"].toString()
                )
            .returning(schemasDictionaryTable.ID)
            .fetchOne()
            .let { it[schemasDictionaryTable.ID]}


        for (attrId in schemaAttributeIds) {
            DSL.using(configuration)
                .insertInto(schemaAttributesTable)
                .columns(schemaAttributesTable.SCHEMA_ID, schemaAttributesTable.ATTRIBUTE_ID)
                .values(newSchemaId, attrId)
                .execute()
        }

        return newSchemaId
    }


    fun updateCurrentSchema(type: SchemaType, body: Any): Map<String, kotlin.Any?> = with(table) {
        val updatedCurrentSchema = provider.tx { configuration ->
            when (type) {
                SchemaType.project_request -> {
                    updateCurrentSchemaProgram(
                        SchemaType.project,
                        duplicateSchemaWithNewType(
                            UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()),
                            SchemaType.project,
                            configuration
                        ),
                        configuration
                    )
                    updateCurrentSchemaProgram(
                        SchemaType.project_request,
                        UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()),
                        configuration
                    )
                }
                else -> updateCurrentSchemaProgram(
                    type,
                    UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()),
                    configuration
                )
            }
        }

        val updated = runBlocking {
            when (val cb = Either.catch { updatedCurrentSchema.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return updated
    }


    fun updateCurrentSchemaForUserProfile(type: SchemaType, body: Any): Map<String, kotlin.Any?> = with(table) {
        val updatedCurrentSchema = provider.tx { configuration ->
            updateCurrentSchemaForUserProfileProgram(type, body, configuration)
        }

        val updated = runBlocking {
            when (val cb = Either.catch { updatedCurrentSchema.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return updated
    }


    fun update(type: SchemaType, body: Any): Map<String, kotlin.Any?> {
        return when (type) {
            SchemaType.student_registration,
            SchemaType.student_profile_template,
            SchemaType.user_registration,
            SchemaType.authorization,
            SchemaType.user_profile_template -> updateCurrentSchemaForUserProfile(type, body)
            else -> updateCurrentSchema(type, body)
        }

    }

    fun getCurrentSchemaProgram(type: SchemaType, configuration: Configuration): MutableList<Map<String, kotlin.Any?>> {
        return DSL.using(configuration)
            .select(COLUMNS_WITH_ID)
            .from(table)
            .where(table.TYPE.eq(type))
            .fetch()
            .fold(mutableListOf()) { list, record ->
                list.add(fetchMapValues(record))

                list
            }
    }

    fun updateCurrentSchemaProgram(type: SchemaType, id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        val oldSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(type))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }


        val updatedCurrentSchema = DSL.using(configuration)
            .update(table)
            .set(table.SCHEMA_ID, id)
            .where(table.TYPE.eq(type))
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .map { fetchMapValues(it) }


        DSL.using(configuration)
            .update(schemaContentTable)
            .set(schemaContentTable.SCHEMA_ID, id)
            .where(schemaContentTable.SCHEMA_ID.eq(oldSchemaId))
            .execute()


        return updatedCurrentSchema
    }


    fun updateCurrentSchemaForUserProfileProgram(type: SchemaType, body: Any, configuration: Configuration): Map<String, kotlin.Any?> {
        val userRegistrationType = when(type) {
            SchemaType.student_registration,
            SchemaType.student_profile_template -> SchemaType.student_registration
            else -> SchemaType.user_registration
        }



        val userProfileTemplateType = when(type) {
            SchemaType.student_registration,
            SchemaType.student_profile_template -> SchemaType.student_profile_template
            else -> SchemaType.user_profile_template
        }

        val userProfileType = when(type) {
            SchemaType.student_registration,
            SchemaType.student_profile_template -> SchemaType.student_profile
            else -> SchemaType.user_profile
        }

        //Update current schema of given type with new schema id
        DSL.using(configuration)
            .update(table)
            .set(table.SCHEMA_ID, UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()))
            .where(table.TYPE.eq(type))
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .map { fetchMapValues(it) }


        val currentUserRegistrationSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(userRegistrationType))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val currentUserProfileTemplateSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(userProfileTemplateType))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val currentUserProfileSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(userProfileType))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val userRegistrationAttributesIds = DSL.using(configuration)
            .select(schemaAttributesTable.ATTRIBUTE_ID)
            .from(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(currentUserRegistrationSchemaId))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[schemaAttributesTable.ATTRIBUTE_ID]
                )

                list
            }

        val userProfileTemplateAttributesIds = DSL.using(configuration)
            .select(schemaAttributesTable.ATTRIBUTE_ID)
            .from(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(currentUserProfileTemplateSchemaId))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[schemaAttributesTable.ATTRIBUTE_ID]
                )

                list
            }

        val attributesMerged: MutableList<UUID> = userRegistrationAttributesIds
            .let { list1 -> userProfileTemplateAttributesIds.let { list2 -> list1 + list2 } }
            .stream().distinct().toList().toMutableList()


        //Get previously-used merged schema as we don't need it anymore
        val oldUserProfileSchema = DSL.using(configuration)
            .select()
            .from(schemasDictionaryTable)
            .where(schemasDictionaryTable.ID.eq(currentUserProfileSchemaId))
            .fetchOne()
            .map{ record ->
                mapOf(
                    "id" to record[schemasDictionaryTable.ID].toString(),
                    "name" to record[schemasDictionaryTable.NAME],
                    "button_name" to record[schemasDictionaryTable.BUTTON_NAME]
                )
            }


        DSL.using(configuration)
            .update(schemasDictionaryTable)
            .set(schemasDictionaryTable.NAME, "not_relevant")
            .where(schemasDictionaryTable.ID.eq(currentUserProfileSchemaId))
            .execute()

        //Create new merged schema
        val newUserProfileSchemaId = DSL.using(configuration)
            .insertInto(schemasDictionaryTable)
            .columns(schemasDictionaryTable.NAME, schemasDictionaryTable.SCHEMA_TYPE, schemasDictionaryTable.BUTTON_NAME)
            .values(oldUserProfileSchema["name"], userProfileType, oldUserProfileSchema["button_name"])
            .returning(schemasDictionaryTable.ID)
            .fetchOne()
            .let{
                it[schemasDictionaryTable.ID]
            }

        //Add attributes to new merged schema
        for (attr in attributesMerged) {
            DSL.using(configuration)
                .insertInto(schemaAttributesTable)
                .columns(schemaAttributesTable.SCHEMA_ID, schemaAttributesTable.ATTRIBUTE_ID)
                .values(newUserProfileSchemaId, attr)
                .execute()
        }

        //Updated all schema-content records with new merged schema
        DSL.using(configuration)
            .update(schemaContentTable)
            .set(schemaContentTable.SCHEMA_ID, newUserProfileSchemaId)
            .where(schemaContentTable.SCHEMA_ID.eq(UUID.fromString(oldUserProfileSchema["id"])))
            .execute()

        //Update current complex schema with new merged schema
        val updatedCurrentComplexSchema = DSL.using(configuration)
            .update(table)
            .set(table.SCHEMA_ID, newUserProfileSchemaId)
            .where(table.TYPE.eq(userProfileType))
            .returning(table.ID, table.SCHEMA_ID, table.TYPE)
            .fetchOne()
            .map { record ->
                fetchMapValues(record)
            }

        val deleteOldSchemaAttributes = DSL.using(configuration)
            .delete(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(UUID.fromString(oldUserProfileSchema["id"])))
            .execute()

        //Delete previously-used merged schema as we don't need it anymore
        val deletedOldUserProfileSchema = DSL.using(configuration)
            .delete(schemasDictionaryTable)
            .where(schemasDictionaryTable.ID.eq(currentUserProfileSchemaId))
            .execute()

        return updatedCurrentComplexSchema
    }


    fun updateCurrentSchemaForStudentProfileProgram(type: SchemaType, body: Any, configuration: Configuration): Map<String, kotlin.Any?> {

        //Update current schema of given type with new schema id
        DSL.using(configuration)
            .update(table)
            .set(table.SCHEMA_ID, UUID.fromString(body[CommonFields.SCHEMA_ID.value].toString()))
            .where(table.TYPE.eq(type))
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .map { fetchMapValues(it) }


        val currentStudentRegistrationSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(SchemaType.student_registration))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val currentStudentProfileTemplateSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(SchemaType.student_profile_template))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val currentStudentProfileSchemaId = DSL.using(configuration)
            .select(table.SCHEMA_ID)
            .from(table)
            .where(table.TYPE.eq(SchemaType.student_profile))
            .fetchOne()
            .let {
                it[table.SCHEMA_ID]
            }

        val studentRegistrationAttributesIds = DSL.using(configuration)
            .select(schemaAttributesTable.ATTRIBUTE_ID)
            .from(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(currentStudentRegistrationSchemaId))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[schemaAttributesTable.ATTRIBUTE_ID]
                )

                list
            }

        val studentProfileTemplateAttributesIds = DSL.using(configuration)
            .select(schemaAttributesTable.ATTRIBUTE_ID)
            .from(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(currentStudentProfileTemplateSchemaId))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(
                    record[schemaAttributesTable.ATTRIBUTE_ID]
                )

                list
            }

        val attributesMerged: MutableList<UUID> = studentRegistrationAttributesIds
            .let { list1 -> studentProfileTemplateAttributesIds.let { list2 -> list1 + list2 } }
            .stream().distinct().toList().toMutableList()


        //Get previously-used merged schema as we don't need it anymore
        val oldStudentProfileSchema = DSL.using(configuration)
            .select(schemasDictionaryTable.ID,schemasDictionaryTable.NAME, schemasDictionaryTable.BUTTON_NAME)
            .from(schemasDictionaryTable)
            .where(schemasDictionaryTable.ID.eq(currentStudentProfileSchemaId))
            .fetchOne()
            .map{ record ->
                mapOf(
                    "id" to record[schemasDictionaryTable.ID].toString(),
                    "name" to record[schemasDictionaryTable.NAME],
                    "button_name" to record[schemasDictionaryTable.BUTTON_NAME]
                )
            }


        DSL.using(configuration)
            .update(schemasDictionaryTable)
            .set(schemasDictionaryTable.NAME, "not_relevant")
            .where(schemasDictionaryTable.ID.eq(currentStudentProfileSchemaId))
            .execute()

        //Create new merged schema
        val newStudentProfileSchemaId = DSL.using(configuration)
            .insertInto(schemasDictionaryTable)
            .columns(schemasDictionaryTable.NAME, schemasDictionaryTable.SCHEMA_TYPE, schemasDictionaryTable.BUTTON_NAME)
            .values(oldStudentProfileSchema["name"], SchemaType.student_profile, oldStudentProfileSchema["button_name"])
            .returning(schemasDictionaryTable.ID)
            .fetchOne()
            .let{
                it[schemasDictionaryTable.ID]
            }

        //Add attributes to new merged schema
        for (attr in attributesMerged) {
            DSL.using(configuration)
                .insertInto(schemaAttributesTable)
                .columns(schemaAttributesTable.SCHEMA_ID, schemaAttributesTable.ATTRIBUTE_ID)
                .values(newStudentProfileSchemaId, attr)
                .execute()
        }

        //Updated all schema-content records with new merged schema
        DSL.using(configuration)
            .update(schemaContentTable)
            .set(schemaContentTable.SCHEMA_ID, newStudentProfileSchemaId)
            .where(schemaContentTable.SCHEMA_ID.eq(UUID.fromString(oldStudentProfileSchema["id"])))
            .execute()

        //Update current complex schema with new merged schema
        val updatedCurrentComplexSchema = DSL.using(configuration)
            .update(table)
            .set(table.SCHEMA_ID, newStudentProfileSchemaId)
            .where(table.TYPE.eq(SchemaType.student_profile))
            .returning(table.ID, table.SCHEMA_ID, table.TYPE)
            .fetchOne()
            .map { record ->
                fetchMapValues(record)
            }

        val deleteOldSchemaAttributes = DSL.using(configuration)
            .delete(schemaAttributesTable)
            .where(schemaAttributesTable.SCHEMA_ID.eq(UUID.fromString(oldStudentProfileSchema["id"])))
            .execute()

        //Delete previously-used merged schema as we don't need it anymore
        val deletedOldStudentProfileSchema = DSL.using(configuration)
            .delete(schemasDictionaryTable)
            .where(schemasDictionaryTable.ID.eq(currentStudentProfileSchemaId))
            .returning(schemasDictionaryTable.ID,schemasDictionaryTable.NAME, schemasDictionaryTable.BUTTON_NAME)
            .fetchOne()
            .map{ record ->
                mapOf(
                    "id" to record[schemasDictionaryTable.ID].toString(),
                    "name" to record[schemasDictionaryTable.NAME],
                    "button_name" to record[schemasDictionaryTable.BUTTON_NAME]
                )
            }

        return updatedCurrentComplexSchema
    }
}
