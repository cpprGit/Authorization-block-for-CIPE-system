package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.crud.formatted.FormattedMailGroupService

object FormattedMailGroupHandler : KoinComponent{

    private val formattedMailGroupsService: FormattedMailGroupService           by inject()

    private val formattedMailGroupDispatcher =
        CRUDDispatcher(formattedMailGroupsService)


    fun dispatch() =
        Handlers.routing()

            //Formatted MailGroups routes
            .add("POST", "/formatted/mail-group/", AuthorizationProvider.sessionWrapper(formattedMailGroupDispatcher::post, CPPW))
            .add("POST", "/formatted/mail-group/{id}/delete", AuthorizationProvider.sessionWrapper(formattedMailGroupDispatcher::delete, CPPW))
            .add("GET", "/formatted/mail-group/{id}", AuthorizationProvider.sessionWrapper(formattedMailGroupDispatcher::get, CPPW))
            .add("POST", "/formatted/mail-group/{id}", AuthorizationProvider.sessionWrapper(formattedMailGroupDispatcher::update, CPPW))
            .add("GET", "/formatted/mail-groups/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getMailGroupsForUser, CPPW))
            .add("GET", "/formatted/mail-groups/users", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getAllUsersForMailGroup, CPPW))

            .add("POST", "/formatted/mail-groups/send-notification/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::sendNotificationToMailGroup, CPPW))
}