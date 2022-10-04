package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.security.SecurityRoles.SUPERVISOR
import ru.hse.cppr.service.crud.ActivitiesService

object ActivityHandler : KoinComponent{

    private val activitiesService: ActivitiesService                            by inject()

    private val activitiesDispatcher = CRUDDispatcher(activitiesService)


    fun dispatch() =
        Handlers.routing()

            // Activity routes
            .add("POST", "/activity", AuthorizationProvider.sessionWrapper(activitiesDispatcher::post, CPPW))
            .add("POST", "/activity/", AuthorizationProvider.sessionWrapper(activitiesDispatcher::post, CPPW))
            .add("GET", "/activity/{id}", AuthorizationProvider.sessionWrapper(activitiesDispatcher::get, ALL_USERS))
            .add("POST", "/activity/{id}", AuthorizationProvider.sessionWrapper(activitiesDispatcher::update, CPPW))
            .add("GET", "/activities/", AuthorizationProvider.sessionWrapper(activitiesDispatcher::list, ALL_USERS))
            .add("DELETE", "/activity/{id}", AuthorizationProvider.sessionWrapper(activitiesDispatcher::delete, SUPERVISOR))

}