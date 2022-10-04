package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.SchemaService

object SchemaHandler : KoinComponent{

    private val schemaService: SchemaService                                                    by inject()

    private val schemaDispatcher = CRUDDispatcher(schemaService)

    fun dispatch() =
        Handlers.routing()

            // Schema routes
            .add("POST", "/schema", AuthorizationProvider.sessionWrapper(schemaDispatcher::post, ALL_USERS))
            .add("POST", "/schema/", AuthorizationProvider.sessionWrapper(schemaDispatcher::post, ALL_USERS))
            .add("GET", "/schemas", AuthorizationProvider.sessionWrapper(schemaDispatcher::list, ALL_USERS))
            .add("GET", "/schemas/", AuthorizationProvider.sessionWrapper(schemaDispatcher::list, ALL_USERS))
            .add("GET", "/schema/{id}", AuthorizationProvider.sessionWrapper(schemaDispatcher::get, ALL_USERS))
            .add("POST", "/schema/{id}", AuthorizationProvider.sessionWrapper(schemaDispatcher::update, ALL_USERS))
            .add("DELETE", "/schema/{id}", AuthorizationProvider.sessionWrapper(schemaDispatcher::delete, ALL_USERS))


}