package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.security.SecurityRoles.SUPERVISOR
import ru.hse.cppr.service.crud.*

object AttributeHandler : KoinComponent{

    private val attributesDictionaryService: AttributesDictionaryService        by inject()

    private val attributeDispatcher = CRUDDispatcher(attributesDictionaryService)

    fun dispatch() =
        Handlers.routing()

            // Attribute routes
            .add("POST", "/attribute", AuthorizationProvider.sessionWrapper(attributeDispatcher::post, CPPW))
            .add("POST", "/attribute/", AuthorizationProvider.sessionWrapper(attributeDispatcher::post, CPPW))
            .add("GET", "/attributes", AuthorizationProvider.sessionWrapper(attributeDispatcher::list, ALL_USERS))
            .add("GET", "/attributes/", AuthorizationProvider.sessionWrapper(attributeDispatcher::list, ALL_USERS))
            .add("GET", "/attribute/{id}", AuthorizationProvider.sessionWrapper(attributeDispatcher::get, ALL_USERS))
            .add("POST", "/attribute/{id}", AuthorizationProvider.sessionWrapper(attributeDispatcher::update, CPPW))
            .add("DELETE", "/attribute/{id}", AuthorizationProvider.sessionWrapper(attributeDispatcher::delete, SUPERVISOR))

}