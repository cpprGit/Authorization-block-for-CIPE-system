package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW_MENTORS
import ru.hse.cppr.security.SecurityRoles.SUPERVISOR
import ru.hse.cppr.service.crud.ProjectService

object ProjectHandler : KoinComponent{

    private val projectService: ProjectService                                  by inject()

    private val projectDispatcher = CRUDDispatcher(projectService)


    fun dispatch() =
        Handlers.routing()
            //Project routes
            .add("GET", "/project/{id}", AuthorizationProvider.sessionWrapper(projectDispatcher::get, ALL_USERS))
            .add("GET", "/projects/", AuthorizationProvider.sessionWrapper(projectDispatcher::list, ALL_USERS))
            .add("POST", "/project/", AuthorizationProvider.sessionWrapper(projectDispatcher::post, CPPW_MENTORS))
            .add("POST", "/project/{id}", AuthorizationProvider.sessionWrapper(projectDispatcher::update, CPPW_MENTORS))
            .add("DELETE", "/project/{id}", AuthorizationProvider.sessionWrapper(projectDispatcher::delete, SUPERVISOR))
}