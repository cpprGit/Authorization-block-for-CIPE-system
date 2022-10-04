package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.StagesService

object StageHandler : KoinComponent{

    private val stagesService: StagesService                                    by inject()

    private val stagesDispatcher = CRUDDispatcher(stagesService)

    fun dispatch() =
        Handlers.routing()

            // Stages routes
            .add("POST", "/stage", AuthorizationProvider.sessionWrapper(stagesDispatcher::post, ALL_USERS))
            .add("POST", "/stage/", AuthorizationProvider.sessionWrapper(stagesDispatcher::post, ALL_USERS))
            .add("GET", "/stage/{id}", AuthorizationProvider.sessionWrapper(stagesDispatcher::get, ALL_USERS))
            .add("POST", "/stage/{id}", AuthorizationProvider.sessionWrapper(stagesDispatcher::update, ALL_USERS))
            .add("GET", "/stages/", AuthorizationProvider.sessionWrapper(stagesDispatcher::list, ALL_USERS))
            .add("DELETE", "/stage/{id}", AuthorizationProvider.sessionWrapper(stagesDispatcher::delete, ALL_USERS))

}