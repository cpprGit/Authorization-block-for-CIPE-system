package ru.hse.cppr.service.notifications

import org.jooq.Configuration
import ru.hse.cppr.service.Service
import java.util.*

interface NotificationsService: Service {

    fun createNotification(notification: Notification, configuration: Configuration)

    fun getNotification(id: UUID): Map<String, Any?>

    fun unreadNotification(id: UUID)

    fun getNotificationsFor(id: UUID): MutableList<Map<String, Any?>>
}