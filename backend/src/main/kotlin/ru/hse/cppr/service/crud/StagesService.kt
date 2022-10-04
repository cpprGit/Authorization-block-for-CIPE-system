package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.*
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.tables.records.StageRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.StageFields
import ru.hse.cppr.utils.`as`
import ru.hse.cppr.utils.asOpt
import ru.hse.cppr.utils.getOpt
import java.sql.Timestamp
import java.util.*

class StagesService(override val serviceName: String) : KoinComponent,
    CRUDService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val table = Tables.STAGE


    val COLUMNS = arrayListOf(
        table.NAME,
        table.DESCRIPTION,
        table.STAGE_NUMBER,
        table.GRADE_COEFFICIENT,
        table.MENTOR_GRADE_FINAL,
        table.START_DATE,
        table.END_DATE
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            StageFields.ID.value to record[table.ID]?.toString(),
            StageFields.NAME.value to record[table.NAME]?.toString(),
            StageFields.DESCRIPTION.value to record[table.DESCRIPTION]?.toString(),
            StageFields.STAGE_NUMBER.value to record[table.STAGE_NUMBER]?.toString(),
            StageFields.GRADE_COEFF.value to record[table.GRADE_COEFFICIENT]?.toString(),
            StageFields.MENTOR_GRADE_FINAL.value to record[table.MENTOR_GRADE_FINAL],
            StageFields.START_DATE.value to record[table.START_DATE]?.toString(),
            StageFields.END_DATE.value to record[table.END_DATE]?.toString()
        )
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val stageM = provider.tx { configuration ->
            createStageProgram(body, configuration)
        }

        val persistedStage = runBlocking {
            when (val cb = Either.catch { stageM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return persistedStage
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val stageListM = provider.tx { configuration ->
            DSL.using(configuration)
                .select(COLUMNS_WITH_ID)
                .from(table)
                .orderBy(table.NAME.asc())
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(
                        fetchMapValues(record)
                    )

                    list
                }
        }

        val foundStageList = runBlocking {
            when (val cb = Either.catch { stageListM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return foundStageList
    }

    override fun get(id: UUID): Map<String, Any?>  = with(table) {
        val stageM = provider.tx { configuration ->
            getStageProgram(id, configuration)
        }

        val stageList = runBlocking {
            when (val cb = Either.catch { stageM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        val res = stageList.firstOrNull()
        when (res) {
            null -> throw BadRequestException("Bad Id.")
            else -> return res
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String = with(table) {
        val stageM = provider.tx { configuration ->

            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<StageRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    StageFields.NAME.value ->
                        statement.set(NAME, body[key].`as`<String>())
                    StageFields.DESCRIPTION.value ->
                        statement.set(DESCRIPTION, body[key].`as`<String>())
                    StageFields.STAGE_NUMBER.value ->
                        statement.set(STAGE_NUMBER, body[key].`as`<Int>())
                    StageFields.GRADE_COEFF.value ->
                        statement.set(GRADE_COEFFICIENT, body[key].`as`<Double>())
                    StageFields.MENTOR_GRADE_FINAL.value ->
                        statement.set(MENTOR_GRADE_FINAL, body[key].`as`<Boolean>())
                    StageFields.START_DATE.value ->
                        statement.set(START_DATE,  Timestamp(DateTime.parse(body[key].toString()).millis))
                    StageFields.END_DATE.value ->
                        statement.set(END_DATE,  Timestamp(DateTime.parse(body[key].toString()).millis))
                    else ->
                        statement
                }
            }

            when (updateStatement) {
                is UpdateSetMoreStep ->
                    updateStatement
                        .where(ID.eq(id))
                        .returning(COLUMNS_WITH_ID)
                        .fetchOptional()
                        .map { record ->
                            fetchMapValues(record)
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
                    log.e("Unable to patch schema.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (stage.isPresent) {
            false -> throw BadRequestException("Task with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(stage.get()))
        }
    }

    override fun delete(id: UUID): String = with (table) {
        val deleteM = provider.tx { configuration ->

            DSL.using(configuration)
                .delete(table)
                .where(ID.eq(id))
                .returning(COLUMNS_WITH_ID)
                .execute()
        }

        runBlocking {
            when (val cb = Either.catch {
                val deleted = deleteM.fix().unsafeRunSync()
                if (deleted == 0) {
                    throw BadRequestException("Bad Id.")
                }
            }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return "{ \"status\": \"Success\"}"
    }

    fun createStageProgram(body: com.jsoniter.any.Any, configuration: Configuration): Map<String, Any?> {
        val startDateString = body[StageFields.START_DATE.value].toString().split(" ")[0]
        val endDateString = body[StageFields.END_DATE.value].toString().split(" ")[0]


        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body[StageFields.NAME.value].`as`<String>(),
                body.getOpt(StageFields.DESCRIPTION.value)?.asOpt(),
                body.getOpt(StageFields.STAGE_NUMBER.value)?.asOpt(),
                body.getOpt(StageFields.GRADE_COEFF.value)?.asOpt(),
                body.getOpt(StageFields.MENTOR_GRADE_FINAL.value)?.asOpt(),
                Timestamp(DateTime.parse(startDateString).millis),
                Timestamp(DateTime.parse(endDateString).millis)
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {
                fetchMapValues(it)
            }
    }

    fun getStageProgram(id: UUID, configuration: Configuration): MutableList<Map<String, Any?>> = with(table){
        return DSL.using(configuration)
            .select(COLUMNS_WITH_ID)
            .from(table)
            .where(ID.eq(id))
            .orderBy(NAME.asc())
            .fetch()
            .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                list.add(
                    fetchMapValues(record)
                )

                list
            }
    }
}