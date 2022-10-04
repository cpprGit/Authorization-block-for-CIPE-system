package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.FileDispatcher
import ru.hse.cppr.routing.dispatchers.MailGroupHistoryDispatcher
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.file.FileService
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService

object MailGroupHistoryHandler : KoinComponent{

    private val service: MailGroupHistoryService by inject()

    private val historyDispatcher = MailGroupHistoryDispatcher(service)

    fun dispatch() =
        Handlers.routing()

            // MG History routes
            .add("POST", "/mail-group/record", AuthorizationProvider.sessionWrapper(historyDispatcher::createRecord, CPPW))
            .add("POST", "/mail-group/records", AuthorizationProvider.sessionWrapper(historyDispatcher::createRecords, CPPW))
            .add("GET", "/mail-group/history", AuthorizationProvider.sessionWrapper(historyDispatcher::getHistory, CPPW))
}