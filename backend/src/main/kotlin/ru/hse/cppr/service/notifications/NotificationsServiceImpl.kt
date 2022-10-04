package ru.hse.cppr.service.notifications

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.NotificationFields
import ru.hse.cppr.representation.formats.ProfileTypes
import java.util.*

class NotificationsServiceImpl(override val serviceName: String) : NotificationsService, KoinComponent {

    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val provider: TxProvider<ForIO>                                         by inject()

    private val notificationsTable = Tables.NOTIFICATIONS
    private val usersTable = Tables.USERS


    val fetchMapValues = { record: Record ->
        val target = if (record[notificationsTable.TARGET_ID] == null) null else mapOf(
            CommonFields.ID.value to record[notificationsTable.TARGET_ID].toString(),
            CommonFields.NAME.value to record[notificationsTable.TARGET_NAME],
            CommonFields.TYPE.value to record[notificationsTable.TARGET_TYPE]
        )

        mapOf(
            NotificationFields.ID.value to record[notificationsTable.ID].toString(),
            NotificationFields.CREATED_BY.value to mapOf(
                CommonFields.ID.value to record[notificationsTable.CREATED_BY].toString(),
                CommonFields.NAME.value to record[usersTable.NAME],
                CommonFields.TYPE.value to ProfileTypes.USER.value
            ),
            NotificationFields.TEXT.value to record[notificationsTable.TEXT]?.toString(),
            NotificationFields.ACTION.value to record[notificationsTable.ACTION]?.toString(),
            NotificationFields.TARGET.value to target,
            NotificationFields.TYPE.value to record[notificationsTable.TYPE]?.toString(),
            NotificationFields.IS_VIEWED.value to record[notificationsTable.IS_VIEWED],
            NotificationFields.DATE.value to record[notificationsTable.DATE].toString()
        )
    }


    override fun createNotification(notification: Notification, configuration: Configuration) {
            DSL.using(configuration)
                .insertInto(notificationsTable)
                .columns(
                    notificationsTable.FOR_USER,
                    notificationsTable.CREATED_BY,
                    notificationsTable.ACTION,
                    notificationsTable.TEXT,
                    notificationsTable.TARGET_ID,
                    notificationsTable.TARGET_NAME,
                    notificationsTable.TARGET_TYPE,
                    notificationsTable.TYPE
                )
                .values(
                    notification.forUser,
                    notification.createdBy,
                    notification.action.value,
                    notification.text,
                    notification.targetId,
                    notification.targetName,
                    notification.targetType,
                    notification.type)
                .returning(notificationsTable.ID)
                .fetchOne()
                .let { it[notificationsTable.ID] }
    }

    override fun getNotification(id: UUID): Map<String, Any?> {
        val notificationM = provider.tx { configuration ->
            val notification =
                DSL.using(configuration)
                    .select()
                    .from(notificationsTable)
                    .innerJoin(usersTable)
                    .on(notificationsTable.CREATED_BY.eq(usersTable.ID))
                    .where(notificationsTable.ID.eq(id))
                    .fetchOne()
                    .map { record ->
                        fetchMapValues(record)
                    }

            val setNotificationAsRead =
                DSL.using(configuration)
                    .update(notificationsTable)
                    .set(notificationsTable.IS_VIEWED, true)
                    .where(notificationsTable.ID.eq(id))
                    .execute()

            notification
        }

        return runBlocking {
            when (val cb = Either.catch { notificationM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun unreadNotification(id: UUID) {
        val notificationM = provider.tx { configuration ->
            val setNotificationAsRead =
                DSL.using(configuration)
                    .update(notificationsTable)
                    .set(notificationsTable.IS_VIEWED, false)
                    .where(notificationsTable.ID.eq(id))
                    .execute()
        }

       runBlocking {
            when (val cb = Either.catch { notificationM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun getNotificationsFor(id: UUID): MutableList<Map<String, Any?>> {
        val notificationM = provider.tx { configuration ->
            DSL.using(configuration)
                .select()
                .from(notificationsTable)
                .innerJoin(usersTable)
                .on(notificationsTable.CREATED_BY.eq(usersTable.ID))
                .where(notificationsTable.FOR_USER.eq(id))
                .orderBy(notificationsTable.IS_VIEWED.asc())
                .fetch()
                .fold(mutableListOf<Map<String, kotlin.Any?>>()) { list, record ->
                    list.add(fetchMapValues(record))

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { notificationM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }
}