package ru.hse.cppr.service.history.mailgroup

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.MgHistoryTargetType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.MailGroupFields
import ru.hse.cppr.representation.enums.fields.MailGroupHistoryFields
import ru.hse.cppr.representation.formats.MailGroupHistoryFormat
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.QuestionnairesService
import ru.hse.cppr.service.file.FileService
import java.sql.Timestamp
import java.util.*

class MailGroupHistoryServiceImpl(override val serviceName: String) : KoinComponent, MailGroupHistoryService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    private val mgHistoryTable = Tables.MAIL_GROUP_HISTORY


    val COLUMNS = arrayListOf(
        mgHistoryTable.MAIL_GROUP,
        mgHistoryTable.CREATED_BY,
        mgHistoryTable.TARGET_ID,
        mgHistoryTable.TARGET_NAME,
        mgHistoryTable.TARGET_TYPE,
        mgHistoryTable.MESSAGE
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, mgHistoryTable.ID) }

    val fetchMapValues = { record: Record ->
        val target = if (record[mgHistoryTable.TARGET_ID] == null) null else mapOf(
            CommonFields.ID.value to record[mgHistoryTable.TARGET_ID].toString(),
            CommonFields.NAME.value to record[mgHistoryTable.TARGET_NAME],
            CommonFields.TYPE.value to record[mgHistoryTable.TARGET_TYPE]
        )

        mapOf(
            MailGroupHistoryFields.ID.value to record[mgHistoryTable.ID]?.toString(),
            MailGroupHistoryFields.MAIL_GROUP.value to record[mgHistoryTable.MAIL_GROUP]?.toString(),
            CommonFields.CREATED_BY.value to record[mgHistoryTable.CREATED_BY]?.toString(),
            MailGroupHistoryFields.TARGET.value to target,
            MailGroupHistoryFields.MESSAGE.value to record[mgHistoryTable.MESSAGE]?.toString(),
            MailGroupHistoryFields.DATE.value to record[mgHistoryTable.DATE]?.toString()
        )
    }


    override fun createRecord(mailGroupId: UUID, body: com.jsoniter.any.Any): String {
        val bodyFormat = MailGroupHistoryFormat(body)

        val historyM = provider.tx { configuration ->
            createRecordProgram(mailGroupId, bodyFormat, configuration)
        }

        runBlocking {
            when (val cb = Either.catch { historyM.fix().unsafeRunSync() }) {
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

    override fun createRecords(mailGroupId: UUID, body: com.jsoniter.any.Any): String {
        val historyM = provider.tx { configuration ->

            for (record in body) {
                createRecordProgram(mailGroupId, MailGroupHistoryFormat(record), configuration)
            }

        }

        runBlocking {
            when (val cb = Either.catch { historyM.fix().unsafeRunSync() }) {
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

    override fun getRecords(mailGroupId: UUID): MutableList<Map<String, Any?>> {
        val historyM = provider.tx { configuration ->

            DSL.using(configuration)
                .select()
                .from(mgHistoryTable)
                .innerJoin(Tables.USERS)
                .on(Tables.USERS.ID.eq(mgHistoryTable.CREATED_BY))
                .where(mgHistoryTable.MAIL_GROUP.eq(mailGroupId))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, record ->

                    val about = if (record[mgHistoryTable.TARGET_ID] == null) null else mapOf(
                        CommonFields.ID.value to record[mgHistoryTable.TARGET_ID]?.toString(),
                        CommonFields.NAME.value to record[mgHistoryTable.TARGET_NAME]?.toString(),
                        CommonFields.TYPE.value to record[mgHistoryTable.TARGET_TYPE]?.toString()
                    )

                    list.add(
                        mapOf(
                            MailGroupHistoryFields.ID.value to record[mgHistoryTable.ID].toString(),
                            MailGroupHistoryFields.MESSAGE.value to record[mgHistoryTable.MESSAGE].toString(),
                            MailGroupHistoryFields.DATE.value to record[mgHistoryTable.DATE].toString(),
                            CommonFields.CREATED_BY.value to mapOf(
                                CommonFields.ID.value to record[mgHistoryTable.CREATED_BY].toString(),
                                CommonFields.NAME.value to record[Tables.USERS.NAME].toString(),
                                CommonFields.TYPE.value to ProfileTypes.USER.value
                            ),

                            MailGroupHistoryFields.TARGET.value to about
                        )
                    )

                    list
                }

        }

        return runBlocking {
            when (val cb = Either.catch { historyM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    override fun createRecordProgram(
        mailGroupId: UUID,
        bodyFormat: MailGroupHistoryFormat,
        configuration: Configuration
    ): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(mgHistoryTable)
            .columns(COLUMNS)
            .values(
                mailGroupId,
                bodyFormat.getCreatedBy(),
                bodyFormat.getTargetId(),
                bodyFormat.getTargetName(),
                bodyFormat.getTargetType(),
                bodyFormat.getMessage()
            )
            .returning()
            .fetchOne()
            .map { fetchMapValues(it) }
    }

    override fun createMailGroupHistoryRecord(id: UUID, body: com.jsoniter.any.Any, targetType: String, configuration: Configuration): MutableList<Map<String, Any>> {
        val mailGroupHistory = body[MailGroupFields.LOGS.value]
        val logs = mutableListOf<Map<String, Any>>()
        for (log in mailGroupHistory) {
            val l = DSL.using(configuration)
                .insertInto(mgHistoryTable)
                .columns(
                    mgHistoryTable.MAIL_GROUP,
                    mgHistoryTable.CREATED_BY,
                    mgHistoryTable.MESSAGE,
                    mgHistoryTable.DATE,
                    mgHistoryTable.TARGET_ID,
                    mgHistoryTable.TARGET_NAME,
                    mgHistoryTable.TARGET_TYPE
                )
                .values(
                    id,
                    UUID.fromString(body[CommonFields.CREATED_BY.value].toString()),
                    log[MailGroupFields.MESSAGE.value].toString(),
                    Timestamp(DateTime.now().millis),
                    UUID.fromString(log[MailGroupFields.TARGET.value][CommonFields.ID.value].toString()),
                    log[MailGroupFields.TARGET.value][CommonFields.NAME.value].toString(),
                    targetType
                )
                .returning()
                .fetchOne()
                .map {
                    mapOf(
                        CommonFields.ID.value to it[mgHistoryTable.ID].toString(),
                        MailGroupFields.TARGET.value to mapOf(
                            CommonFields.ID.value to it[mgHistoryTable.TARGET_ID].toString(),
                            CommonFields.NAME.value to it[mgHistoryTable.TARGET_NAME],
                            CommonFields.TYPE.value to it[mgHistoryTable.TARGET_TYPE]
                        ),
                        MailGroupFields.DATE.value to it[mgHistoryTable.DATE].toString(),
                        MailGroupFields.MESSAGE.value to it[mgHistoryTable.MESSAGE]
                    )
                }

            logs.add(l)
        }
        return logs
    }


}