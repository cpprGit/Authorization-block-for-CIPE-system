package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.any.Any
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
import ru.hse.cppr.data.database_generated.tables.records.AttributesRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.AttributeFormat
import ru.hse.cppr.representation.enums.fields.AttributeFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.utils.*
import java.util.*
import kotlin.collections.ArrayList

class AttributesDictionaryService(override val serviceName: String) : KoinComponent,
    CRUDService {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.ATTRIBUTES
    private val variantsTable = Tables.VARIANTS
    private val attrVariantsTable = Tables.ATTRIBUTE_VARIANTS

    private val validatorsTable = Tables.VALIDATORS
    private val attrValidatorsTable = Tables.ATTRIBUTE_VALIDATORS

    val COLUMNS = arrayListOf(
        table.USAGE,
        table.NAME,
        table.DESCRIPTION,
        table.TITLE,
        table.PLACEHOLDER,
        table.STEP,
        table.MIN,
        table.MAX,
        table.HINT,
        table.MANDATORY,
        table.VALUE_DEFAULT,
        table.SEARCH_NAME,
        table.HAS_OTHER_VARIANT
    )
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            AttributeFields.ID.value to record[table.ID].toString(),
            AttributeFields.USAGE.value to record[table.USAGE].toString(),
            AttributeFields.NAME.value to record[table.NAME],
            AttributeFields.DESCRIPTION.value to record[table.DESCRIPTION],
            AttributeFields.TITLE.value to record[table.TITLE],
            AttributeFields.PLACEHOLDER.value to record[table.PLACEHOLDER],
            AttributeFields.STEP.value to record[table.STEP],
            AttributeFields.MIN.value to record[table.MIN],
            AttributeFields.MAX.value to record[table.MAX],
            AttributeFields.HINT.value to record[table.HINT],
            AttributeFields.MANDATORY.value to record[table.MANDATORY],
            AttributeFields.VALUE_DEFAULT.value to record[table.VALUE_DEFAULT],
            AttributeFields.SEARCH_NAME.value to record[table.SEARCH_NAME],
            AttributeFields.HAS_OTHER_VARIANT.value to record[table.HAS_OTHER_VARIANT]
        )
    }


    override fun create(body: Any): Map<String, kotlin.Any> = with(table){
        log.i("Trying to save attribute \'$body\'")

        val attributeBody = AttributeFormat(body)

        val attrM = provider.tx { configuration ->

            val variantsIds = ArrayList<UUID>()
            for(variant in attributeBody.getVariants()) {
                val v = DSL.using(configuration)
                    .insertInto(variantsTable)
                    .columns(variantsTable.VARIANT)
                    .values(variant.toString())
                    .returning(variantsTable.ID)
                    .fetchOne()
                    .let { it[variantsTable.ID] }

                variantsIds.add(v)
            }

            val validatorsIds = ArrayList<UUID>()
            for(validator in attributeBody.getValidators()) {
                val v = DSL.using(configuration)
                    .insertInto(validatorsTable)
                    .columns(validatorsTable.VALIDATOR, validatorsTable.MESSAGE)
                    .values(validator["regexp"].toString(), validator["message"].toString())
                    .returning(validatorsTable.ID)
                    .fetchOne()
                    .let { it[validatorsTable.ID] }

                validatorsIds.add(v)
            }

            val attrId = DSL.using(configuration)
                .insertInto(table)
                .columns(COLUMNS)
                .values(
                    attributeBody.getUsage(),
                    attributeBody.getName(),
                    attributeBody.getDescription(),
                    attributeBody.getTitle(),
                    attributeBody.getPlaceholder(),
                    attributeBody.getStep(),
                    attributeBody.getMin(),
                    attributeBody.getMax(),
                    attributeBody.getHint(),
                    attributeBody.getMandatory(),
                    attributeBody.getValueDefault(),
                    null,
                    attributeBody.getHasOtherVariant()
                )
                .returning(COLUMNS_WITH_ID)
                .fetchOne()
                .let {
                    it[table.ID]
                }

            for (id in variantsIds) {
                DSL.using(configuration)
                    .insertInto(attrVariantsTable)
                    .columns(attrVariantsTable.ATTRIBUTE_ID, attrVariantsTable.VARIANT_ID)
                    .values(attrId, id)
                    .returning(attrVariantsTable.ID)
                    .fetchOne()
            }

            for (id in validatorsIds) {
                DSL.using(configuration)
                    .insertInto(attrValidatorsTable)
                    .columns(attrValidatorsTable.ATTRIBUTE_ID, attrValidatorsTable.VALIDATOR_ID)
                    .values(attrId, id)
                    .returning(attrValidatorsTable.ID)
                    .fetchOne()
            }

            getAttributeProgram(configuration, attrId)

        }

        val persistedAttribute = runBlocking {
            when (val cb = Either.catch { attrM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to create new attribute.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Attribute creation success. \'$body\'")
                    return@runBlocking cb.b
                }
            }
        }

        return persistedAttribute
    }



    override fun list(vararg params: String): MutableList<Map<String, kotlin.Any?>> = with(table) {
        val attrListM = provider.tx { configuration ->
           val attributesIds =  DSL.using(configuration)
                .select(table.ID)
                .from(table)
                .orderBy(NAME.asc())
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[table.ID])
                    list
                }

            val attributes = ArrayList<Map<String, kotlin.Any?>>()
            for (attrId in attributesIds) {
                attributes.add(getAttributeProgram(configuration, attrId))
            }

            attributes
        }

        val attrList = runBlocking {
            when (val cb = Either.catch { attrListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to retrieve attribute list.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Get attribute list success.")
                    return@runBlocking cb.b
                }
            }
        }

        return attrList.toMutableList()
    }


    override fun get(id : UUID): Map<String, kotlin.Any?> = with(table) {
        val attrListM = provider.tx { configuration ->
            getAttributeProgram(configuration, id)
        }

        val attribute = runBlocking {
            when (val cb = Either.catch { attrListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to retrieve attribute.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Get attribute success.")
                    return@runBlocking cb.b
                }
            }
        }

        when (attribute.isEmpty()) {
            true -> throw BadRequestException("Bad Id.")
            else -> return attribute
        }
    }

    override fun update(id: UUID, body: Any) : String = with(table) {
        val usageM = provider.tx { configuration ->

            val updateStatement = body.keys()
                .fold<kotlin.Any?, UpdateSetStep<AttributesRecord>>(DSL.using(configuration).update(table)) { statement, key ->
                    when (val key = key.toString()) {
                        USAGE.name ->
                            statement.set(USAGE, body[key].`as`<String>())
                        NAME.name ->
                            statement.set(NAME, body[key].`as`<String>())
                        TITLE.name ->
                            statement.set(TITLE, body[key].`as`<String>())
                        PLACEHOLDER.name ->
                            statement.set(PLACEHOLDER, body[key].asOpt<String>())
                        STEP.name ->
                            statement.set(STEP, body[key].asOpt<Int>())
                        MIN.name ->
                            statement.set(MIN, body[key].asOpt<Int>())
                        MAX.name ->
                            statement.set(MAX, body[key].asOpt<Int>())
                        HINT.name ->
                            statement.setOpt(HINT, body[key].asOpt())
                        MANDATORY.name ->
                            statement.setOpt(MANDATORY, body[key].asOpt())
                        VALUE_DEFAULT.name ->
                            statement.setOpt(VALUE_DEFAULT, body[key].asOpt())
                        HAS_OTHER_VARIANT.name ->
                            statement.setOpt(HAS_OTHER_VARIANT, body[key].asOpt())
                        "validators" ->  {
                            updateValidatorsProgram(id, body, configuration)
                            statement.set(ID, ID)
                        }
                        "variants" ->  {
                            updateVariantsProgram(id, body, configuration)
                            statement.set(ID, ID)
                        }

                        else ->
                            statement
                    }
                }

            when (updateStatement) {
                is UpdateSetMoreStep -> {
                    updateStatement
                        .where(ID.eq(id))
                        .returning(COLUMNS_WITH_ID)
                        .fetchOptional()
                        .map { record ->
                            mapOf(
                                "id" to ID
                            )
                        }
                    getAttributeProgram(configuration, id)
                }

                else ->
                    null
            }
        }

        val usage = runBlocking {
            when (val cb = Either.catch { usageM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to patch attribute.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    log.i("Attribute patch success. \'$body\'")
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(Any.wrap(usage))
    }


    override fun delete(id: UUID) : String = with(table) {
        val attrsM = provider.tx { configuration ->

            DSL.using(configuration)
                .deleteFrom(attrVariantsTable)
                .where(attrVariantsTable.ATTRIBUTE_ID.eq(id))
                .execute()

            DSL.using(configuration)
                .deleteFrom(attrValidatorsTable)
                .where(attrValidatorsTable.ATTRIBUTE_ID.eq(id))
                .execute()

            DSL.using(configuration)
                .deleteFrom(table)
                .where(ID.eq(id))
                .returning(ID, NAME)
                .execute()
        }

        val deletedRecord = runBlocking {
            when (val cb = Either.catch {
                val deleted = attrsM.fix().unsafeRunSync()
                if (deleted == 0) {
                    throw BadRequestException("Bad id.")
                }
            }) {
                is Either.Left -> {
                    log.e("Unable to delete attribute record.")
                    throw BadRequestException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(Any.wrap(deletedRecord))
    }



    fun getAttributeProgram(configuration: Configuration, id: UUID) : MutableMap<String, kotlin.Any>  = with(table) {
        val attr = DSL.using(configuration)
            .select(COLUMNS_WITH_ID)
            .from(table)
            .where(ID.eq(id))
            .orderBy(NAME.asc())
            .fetchOne()
            .map { record ->
                fetchMapValues(record)
            }.toMutableMap()


        val variants_ids = DSL.using(configuration)
            .select(attrVariantsTable.VARIANT_ID)
            .from(attrVariantsTable)
            .where(attrVariantsTable.ATTRIBUTE_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[attrVariantsTable.VARIANT_ID])

                list
            }

        val variants = ArrayList<String>()

        for (var_id in variants_ids) {
            val variant = DSL.using(configuration)
                .select(variantsTable.VARIANT)
                .from(variantsTable)
                .where(variantsTable.ID.eq(var_id))
                .fetchOne()
                .let {
                    it[variantsTable.VARIANT]
                }

            variants.add(variant)
        }

        val validators_ids = DSL.using(configuration)
            .select(attrValidatorsTable.VALIDATOR_ID)
            .from(attrValidatorsTable)
            .where(attrValidatorsTable.ATTRIBUTE_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[attrValidatorsTable.VALIDATOR_ID])

                list
            }

        val validators = ArrayList<Map<String, String>>()

        for (var_id in validators_ids) {
            val validator = DSL.using(configuration)
                .select(validatorsTable.VALIDATOR, validatorsTable.MESSAGE)
                .from(validatorsTable)
                .where(validatorsTable.ID.eq(var_id))
                .fetchOne()
                .map { record ->
                    mapOf(
                        "regexp" to record[validatorsTable.VALIDATOR],
                        "message" to record[validatorsTable.MESSAGE]
                    )
                }

            validators.add(validator)
        }

        attr["variants"] = variants
        attr["validators"] = validators

        return attr
    }


    fun updateVariantsProgram(id: UUID, body: Any, configuration: Configuration) {
        val deletedAtrributeVariants = DSL.using(configuration)
            .deleteFrom(attrVariantsTable)
            .where(attrVariantsTable.ATTRIBUTE_ID.eq(id))
            .returning(attrVariantsTable.VARIANT_ID)
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[attrVariantsTable.VARIANT_ID])

                list
            }

        for (varId in deletedAtrributeVariants) {
            DSL.using(configuration)
                .deleteFrom(variantsTable)
                .where(variantsTable.ID.eq(varId))
                .execute()
        }

        val variants_ids = ArrayList<UUID>()
        for(variant in body["variants"]) {
            val v = DSL.using(configuration)
                .insertInto(variantsTable)
                .columns(variantsTable.VARIANT)
                .values(variant.toString())
                .returning(variantsTable.ID)
                .fetchOne()
                .let { it[variantsTable.ID] }

            variants_ids.add(v)
        }

        for (var_id in variants_ids) {
            DSL.using(configuration)
                .insertInto(attrVariantsTable)
                .columns(attrVariantsTable.ATTRIBUTE_ID, attrVariantsTable.VARIANT_ID)
                .values(id, var_id)
                .returning(attrVariantsTable.ID)
                .fetchOne()
        }

    }


    fun updateValidatorsProgram(id: UUID, body: Any, configuration: Configuration) {
        val deletedAtrributeValidators = DSL.using(configuration)
            .deleteFrom(attrValidatorsTable)
            .where(attrValidatorsTable.ATTRIBUTE_ID.eq(id))
            .returning(attrValidatorsTable.VALIDATOR_ID)
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[attrValidatorsTable.VALIDATOR_ID])

                list
            }

        for (varId in deletedAtrributeValidators) {
            DSL.using(configuration)
                .deleteFrom(validatorsTable)
                .where(validatorsTable.ID.eq(varId))
                .execute()
        }

        val validators_ids = ArrayList<UUID>()
        for(validator in body["validators"]) {
            val v = DSL.using(configuration)
                .insertInto(validatorsTable)
                .columns(validatorsTable.VALIDATOR, validatorsTable.MESSAGE)
                .values(validator["regexp"].toString(), validator["message"].toString())
                .returning(validatorsTable.ID)
                .fetchOne()
                .let { it[validatorsTable.ID] }

            validators_ids.add(v)
        }

        for (val_id in validators_ids) {
            DSL.using(configuration)
                .insertInto(attrValidatorsTable)
                .columns(attrValidatorsTable.ATTRIBUTE_ID, attrValidatorsTable.VALIDATOR_ID)
                .values(id, val_id)
                .returning(attrValidatorsTable.ID)
                .fetchOne()
        }
    }


}