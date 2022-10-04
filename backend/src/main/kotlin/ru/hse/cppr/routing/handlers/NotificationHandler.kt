package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.NotificationsDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.notifications.NotificationsService

object NotificationHandler : KoinComponent{

    private val notificationsService: NotificationsService                            by inject()

    private val notificationsDispatcher = NotificationsDispatcher(notificationsService)


    fun dispatch() =
        Handlers.routing()

            // Notifications routes
            .add("GET", "/notification/{id}", AuthorizationProvider.sessionWrapper(notificationsDispatcher::getNotification, ALL_USERS))
            .add("POST", "/notification/unread/{id}", AuthorizationProvider.sessionWrapper(notificationsDispatcher::unreadNotification, ALL_USERS))
            .add("GET", "/notifications/user/{id}", AuthorizationProvider.sessionWrapper(notificationsDispatcher::getNotificationsFor, ALL_USERS))
}