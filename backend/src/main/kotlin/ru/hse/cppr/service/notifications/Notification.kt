package ru.hse.cppr.service.notifications

import org.joda.time.DateTime
import ru.hse.cppr.data.database_generated.enums.NotificationTargetType
import ru.hse.cppr.data.database_generated.enums.NotificationType
import java.util.*


data class Notification(val forUser: UUID, val createdBy: UUID, val action: NotificationActions, val type: NotificationType) {

    var text: String? = null

    var targetId: UUID? = null
    var targetName: String? = null
    var targetType: NotificationTargetType? = null

    var isViewed: Boolean = false
}