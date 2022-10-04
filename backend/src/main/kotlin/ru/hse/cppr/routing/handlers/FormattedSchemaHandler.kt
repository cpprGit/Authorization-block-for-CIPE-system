package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService

object FormattedSchemaHandler : KoinComponent{

    private val formattedSchemaService: FormattedSchemaService                  by inject()

    private val formattedSchemaDispatcher = CRUDDispatcher(formattedSchemaService)


    fun dispatch() =
        Handlers.routing()

            //Formatted Schema routes
            .add("GET", "/formatted/schemas/", AuthorizationProvider.sessionWrapper(formattedSchemaDispatcher::list, ALL_USERS))
            .add("GET", "/formatted/schema/{id}", AuthorizationProvider.sessionWrapper(formattedSchemaDispatcher::get, ALL_USERS))
            .add("POST", "/formatted/schema/", AuthorizationProvider.sessionWrapper(formattedSchemaDispatcher::post, CPPW))
            .add("POST", "/formatted/schema/{id}", AuthorizationProvider.sessionWrapper(formattedSchemaDispatcher::update, CPPW))
            .add("DELETE", "/formatted/schema/{id}", AuthorizationProvider.sessionWrapper(formattedSchemaDispatcher::delete, CPPW))

            .add("POST", "/formatted/schema/set-archived", AuthorizationProvider.sessionWrapper(FormattedDispatcher::setSchemaArchived, CPPW))
            .add("GET", "/formatted/schemas/archived", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getArchivedSchemas, ALL_USERS))

}