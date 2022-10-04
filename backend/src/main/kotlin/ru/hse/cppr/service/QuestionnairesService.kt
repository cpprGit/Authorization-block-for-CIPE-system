package ru.hse.cppr.service

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.MgHistoryTargetType
import ru.hse.cppr.data.database_generated.enums.NotificationTargetType
import ru.hse.cppr.data.database_generated.enums.NotificationType
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.dependencies.mailGroupHistoryService
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.QuestionnaireFormat
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.MailGroupFields
import ru.hse.cppr.representation.enums.fields.QuestionnaireFields
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.service.crud.MailGroupsService
import ru.hse.cppr.service.crud.SchemaContentService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService
import ru.hse.cppr.service.notifications.Notification
import ru.hse.cppr.service.notifications.NotificationActions
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.utils.getFields
import ru.hse.cppr.utils.getManagersAndSupervisorIds
import java.sql.Timestamp
import java.util.*

object QuestionnairesService : KoinComponent {

    private val provider: TxProvider<ForIO>                                   by inject()

    private val schemaContentService: SchemaContentService                    by inject()
    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val notificationsService: NotificationsService                    by inject()

    private val mailGroupHistoryService: MailGroupHistoryService              by inject()


    private val usersMailGroupTable = Tables.USER_MAIL_GROUP
    private val questionnairesTable = Tables.QUESTIONAIRES
    private val schemasTable = Tables.SCHEMAS_DICTIONARY
    private val schemaContentTable = Tables.SCHEMA_CONTENT
    private val mgHistoryTable = Tables.MAIL_GROUP_HISTORY


    fun getAllQuestionnairesForms(): MutableList<Map<String, kotlin.Any?>> {
        val programM = provider.tx { configuration ->
            DSL.using(configuration)
                .selectFrom(schemasTable)
                .where(schemasTable.SCHEMA_TYPE.eq(SchemaType.questionnaire))
                .fetch()
                .fold( mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add (mapOf(
                        CommonFields.ID.value to record[schemasTable.ID].toString(),
                        CommonFields.NAME.value to record[schemasTable.NAME].toString()
                    ))
                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { programM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    fun createQuestionnairesFromMailGroup(body: Any): List<Map<String, kotlin.Any>> {
        val formatBody = QuestionnaireFormat(body)

        val program = provider.tx { configuration ->
            val userIds = DSL.using(configuration)
                .selectFrom(usersMailGroupTable)
                .where(usersMailGroupTable.MAIL_GROUP_ID.eq(formatBody.getMailGroupId()))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[usersMailGroupTable.USER_ID])
                    list
                }

            val schemaName = DSL.using(configuration)
                .selectFrom(schemasTable)
                .where(schemasTable.ID.eq(formatBody.getSchemaId()))
                .fetchOne()
                .let { it[schemasTable.NAME].toString() }

            for (userId in userIds) {
                val newSchemaContent =
                    schemaContentService.createProgram(createSchemaContentBody(formatBody.getSchemaId()), configuration)

                val newQuestionnaire = DSL.using(configuration)
                    .insertInto(questionnairesTable)
                    .columns(
                        questionnairesTable.NAME,
                        questionnairesTable.FILL_BY,
                        questionnairesTable.IS_FILLED,
                        questionnairesTable.SCHEMA_CONTENT_ID
                    )
                    .values(
                        schemaName,
                        userId,
                        false,
                        UUID.fromString(newSchemaContent[CommonFields.ID.value].toString())
                    )
                    .returning()
                    .fetchOne()
                    .map {
                        mapOf(
                            CommonFields.ID.value to it[questionnairesTable.ID],
                            CommonFields.NAME.value to it[questionnairesTable.NAME]
                        )
                    }


                val notification = Notification(
                    userId,
                    UUID.fromString(body[CommonFields.CREATED_BY.value].toString()),
                    NotificationActions.CREATED_QUESTIONNAIRE,
                    NotificationType.profile
                )

                notification.targetId = newQuestionnaire[CommonFields.ID.value] as UUID?
                notification.targetName = newQuestionnaire[CommonFields.NAME.value] as String?
                notification.targetType = null

                notificationsService.createNotification(notification, configuration)
            }

            val logs = mutableListOf<Map<String, kotlin.Any>>()
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
                    formatBody.getMailGroupId(),
                    UUID.fromString(body[CommonFields.CREATED_BY.value].toString()),
                    "Отправлен опрос",
                    Timestamp(DateTime.now().millis),
                    formatBody.getSchemaId(),
                    schemaName,
                    "forms"
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

            logs
        }

        return runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    fun getQuestionnairesByUserId(id: UUID): MutableMap<String, kotlin.Any> {
        val program = provider.tx { configuration ->
            val questionnaires = DSL.using(configuration)
                .select(
                    questionnairesTable.ID,
                    questionnairesTable.NAME,
                    questionnairesTable.FILL_BY,
                    questionnairesTable.IS_FILLED,
                    schemaContentTable.SCHEMA_ID,
                    schemaContentTable.CONTENT
                )
                .from(questionnairesTable)
                .innerJoin(schemaContentTable)
                .on(questionnairesTable.SCHEMA_CONTENT_ID.eq(schemaContentTable.ID))
                .where(questionnairesTable.FILL_BY.eq(id))
                .orderBy(questionnairesTable.IS_FILLED)
                .fetch()
                .fold (mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(mapOf(
                        QuestionnaireFields.ID.value to record[questionnairesTable.ID].toString(),
                        QuestionnaireFields.NAME.value to record[questionnairesTable.NAME].toString(),
                        QuestionnaireFields.FILL_BY.value to record[questionnairesTable.FILL_BY].toString(),
                        QuestionnaireFields.IS_FILLED.value to record[questionnairesTable.IS_FILLED].toString(),
                        QuestionnaireFields.SCHEMA.value to formattedSchemaService.getWithoutFieldsProgram(configuration, record[schemaContentTable.SCHEMA_ID]),
                        QuestionnaireFields.CONTENT.value to record[schemaContentTable.CONTENT].toString()
                    ))

                    list
                }

            val fields = getFields(configuration, SchemaType.questionnaire)

            val result = mutableMapOf<String, kotlin.Any>()

            result["fields"] = fields
            result["records"] = questionnaires

            result
        }

        return runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    fun getQuestionnaireProfile(id: UUID): MutableMap<String, kotlin.Any?>? {
        val program = provider.tx { configuration ->
            val questionnaire = DSL.using(configuration)
                .select(
                    questionnairesTable.ID,
                    questionnairesTable.NAME,
                    questionnairesTable.FILL_BY,
                    questionnairesTable.IS_FILLED,
                    schemaContentTable.SCHEMA_ID,
                    schemaContentTable.CONTENT
                )
                .from(questionnairesTable)
                .innerJoin(schemaContentTable)
                .on(questionnairesTable.SCHEMA_CONTENT_ID.eq(schemaContentTable.ID))
                .where(questionnairesTable.ID.eq(id))
                .fetchOne()
                .map { record ->
                    val questionnaireM = mapOf(
                        QuestionnaireFields.NAME.value to record[questionnairesTable.NAME].toString(),
                        QuestionnaireFields.FILL_BY.value to record[questionnairesTable.FILL_BY].toString(),
                        QuestionnaireFields.IS_FILLED.value to record[questionnairesTable.IS_FILLED].toString(),
                        QuestionnaireFields.CONTENT.value to record[schemaContentTable.CONTENT].toString()
                    )

                    val result = formattedSchemaService.getWithoutFieldsProgram(
                        configuration,
                        record[schemaContentTable.SCHEMA_ID]
                    ) as MutableMap

                    for (key in questionnaireM.keys) {
                        result[key] = questionnaireM[key]
                    }

                    result
                }


            questionnaire["fields"] = getFields(configuration, SchemaType.questionnaire)

            questionnaire
        }

        return runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    private fun createSchemaContentBody(id: UUID): Any {
        return JsonIterator.deserialize(
            JsonStream.serialize(
                Any.wrap(
                    mapOf(
                        CommonFields.SCHEMA_ID.value to id.toString(),
                        CommonFields.SCHEMA_CONTENT.value to ""
                    )
                )
            )
        )
    }

}