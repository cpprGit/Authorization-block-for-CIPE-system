package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.SchemaContentService

object FormHandler : KoinComponent{

    private val schemaContentService: SchemaContentService                      by inject()

    private val formDispatcher = CRUDDispatcher(schemaContentService)

    fun dispatch() =
        Handlers.routing()

            // Form routes
            .add("POST", "/form", AuthorizationProvider.sessionWrapper(formDispatcher::post, ALL_USERS))
            .add("POST", "/form/", AuthorizationProvider.sessionWrapper(formDispatcher::post, ALL_USERS))
            .add("GET", "/forms", AuthorizationProvider.sessionWrapper(formDispatcher::list, ALL_USERS))
            .add("GET", "/forms/", AuthorizationProvider.sessionWrapper(formDispatcher::list, ALL_USERS))
            .add("GET", "/form/{id}", AuthorizationProvider.sessionWrapper(formDispatcher::get, ALL_USERS))
            .add("POST", "/form/{id}", AuthorizationProvider.sessionWrapper(formDispatcher::update, ALL_USERS))
            .add("DELETE", "/form/{id}", AuthorizationProvider.sessionWrapper(formDispatcher::delete, ALL_USERS))

}