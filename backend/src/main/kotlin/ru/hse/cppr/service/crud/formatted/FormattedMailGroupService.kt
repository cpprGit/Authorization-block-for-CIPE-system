package ru.hse.cppr.service.crud.formatted

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.MgHistoryTargetType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.MailGroupFormat
import ru.hse.cppr.representation.enums.fields.MailGroupFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.DefaultAttributesService
import ru.hse.cppr.service.crud.*
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService
import ru.hse.cppr.service.profile.UserProfileService
import java.sql.Timestamp
import java.util.*

class FormattedMailGroupService(override val serviceName: String): KoinComponent, CRUDService {

    val provider: TxProvider<ForIO>                                     by inject()
    val log: Log                                                        by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val mailGroupsService: MailGroupsService                            by inject()
    private val userProfileService: UserProfileService                          by inject()

    private val mailGroupHistoryService: MailGroupHistoryService                by inject()


    private val mailGroupTable = Tables.MAIL_GROUP
    private val userMailGroupTable = Tables.USER_MAIL_GROUP
    private val usersTable = Tables.USERS
    private val mailGroupHistoryTable = Tables.MAIL_GROUP_HISTORY


    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val mailGroupM = provider.tx { configuration ->
            val mailGroupBody = MailGroupFormat(body)
            val mailGroupHistory = body[MailGroupFields.LOGS.value]

            val mailGroup = mailGroupsService.createMailGroupProgram(mailGroupBody, configuration) as MutableMap
            val mailGroupId = UUID.fromString(mailGroup[MailGroupFields.ID.value].toString())

            for (user in mailGroupBody.getUsers()) {
                val addUsersToMailGroup = DSL.using(configuration)
                    .insertInto(userMailGroupTable)
                    .columns(userMailGroupTable.USER_ID, userMailGroupTable.MAIL_GROUP_ID)
                    .values(UUID.fromString(user.toString()), mailGroupId)
                    .execute()
            }

            for (log in mailGroupHistory) {
                DSL.using(configuration)
                    .insertInto(mailGroupHistoryTable)
                    .columns(
                        mailGroupHistoryTable.MAIL_GROUP,
                        mailGroupHistoryTable.CREATED_BY,
                        mailGroupHistoryTable.MESSAGE,
                        mailGroupHistoryTable.DATE,
                        mailGroupHistoryTable.TARGET_ID,
                        mailGroupHistoryTable.TARGET_NAME,
                        mailGroupHistoryTable.TARGET_TYPE
                    )
                    .values(
                        mailGroupId,
                        UUID.fromString(body[CommonFields.CREATED_BY.value].toString()),
                        log[MailGroupFields.MESSAGE.value].toString(),
                        Timestamp(DateTime.now().millis),
                        null,
                        null,
                        null
                    )
                    .execute()
            }

            mailGroup[CommonFields.MODIFY_ALLOWED.value] = true
            mailGroup

        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    fun listByUserId(id: UUID): MutableMap<String, Any?> {
        val mailGroupsM = provider.tx { configuration ->
            val mailGroupsIds = DSL.using(configuration)
                .selectFrom(mailGroupTable)
                .where(mailGroupTable.CREATED_BY.eq(id))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[mailGroupTable.ID])
                    list
                }

            val groups = mutableListOf<Map<String, Any?>>()

            for (mailGroupId in mailGroupsIds) {
                groups.add(getFormattedMailGroupProgramWithoutFields(mailGroupId, configuration))
            }

            val result = mutableMapOf<String, Any?>()

            result[MailGroupFields.GROUPS.value] = groups
            result[MailGroupFields.MAIL_FIELDS.value] = addMailGroupFields()

            result
        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun get(id: UUID): Map<String, Any?> {
        val mailGroupM = provider.tx { configuration ->
            getFormattedMailGroupProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        val txResult = provider.tx { configuration ->
            DSL.using(configuration)
                .deleteFrom(userMailGroupTable)
                .where(userMailGroupTable.MAIL_GROUP_ID.eq(id))
                .execute()

            val mailGroupBody = MailGroupFormat(body)

            for (user in mailGroupBody.getUsers()) {
                val addUsersToMailGroup = DSL.using(configuration)
                    .insertInto(userMailGroupTable)
                    .columns(userMailGroupTable.USER_ID, userMailGroupTable.MAIL_GROUP_ID)
                    .values(UUID.fromString(user.toString()), id)
                    .execute()
            }

            mailGroupHistoryService.createMailGroupHistoryRecord(id, body, ProfileTypes.USER.value, configuration)
            "{}"
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun delete(id: UUID): String {
        val txResult = provider.tx {  configuration ->

            DSL.using(configuration)
                .deleteFrom(userMailGroupTable)
                .where(userMailGroupTable.MAIL_GROUP_ID.eq(id))
                .execute()

            DSL.using(configuration)
                .deleteFrom(mailGroupHistoryTable)
                .where(mailGroupHistoryTable.MAIL_GROUP.eq(id))
                .execute()


            DSL.using(configuration)
                .deleteFrom(mailGroupTable)
                .where(mailGroupTable.ID.eq(id))
                .execute()

                "{}"
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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

    fun getFormattedMailGroupProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        val result = getFormattedMailGroupProgramWithoutFields(id, configuration)
        result[MailGroupFields.MAIL_FIELDS.value] = addMailGroupFields()

        return result
    }


    private fun getFormattedMailGroupProgramWithoutFields(id: UUID, configuration: Configuration): MutableMap<String, Any?> {
        val mailGroup = mailGroupsService.getMailGroupProgram(id, configuration) as MutableMap

        val groupUsersIds = DSL.using(configuration)
            .selectFrom(userMailGroupTable)
            .where(userMailGroupTable.MAIL_GROUP_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(UUID.fromString(record[userMailGroupTable.USER_ID.name].toString()))
                list
            }

        val users = mutableListOf<Map<String, Any?>>()

        for (userId in groupUsersIds) {
            val userType = DSL.using(configuration)
                .selectFrom(usersTable)
                .where(usersTable.ID.eq(userId))
                .fetchOne()
                .let { it[usersTable.TYPE] }

            users.add(userProfileService.getUserInfo(userType, userId, configuration))
        }

        mailGroup[MailGroupFields.USERS.value] = users

        return mailGroup
    }

    fun addMailGroupFields(): Map<String, Any> {
        val fields = mutableMapOf<String, Any>()

        fields["student"] = DefaultAttributesService.getDefaultFieldsForSchemaType("student_registration")
        (fields["student"] as MutableList<Map<String, Any?>>).removeIf { field ->
            val attr = field["attribute"] as Map<String, Any?>
            attr["usage"].toString().toLowerCase() == "password"
        }

        return fields
    }

}