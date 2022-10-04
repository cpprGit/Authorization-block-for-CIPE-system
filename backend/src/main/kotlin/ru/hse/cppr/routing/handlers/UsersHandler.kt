package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import io.undertow.server.RoutingHandler
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.routing.dispatchers.AuthorizationDispatcher
import ru.hse.cppr.routing.dispatchers.SignUpDispatcher
import ru.hse.cppr.service.users.UsersService

object UsersHandler : KoinComponent{

    private val usersService: UsersService                         by inject()

    private val dispatcher_sign_up = SignUpDispatcher(usersService)
    private val dispatcher_sign_in = AuthorizationDispatcher(usersService)


    fun dispatch(): RoutingHandler =
        Handlers.routing()
            // User registration routes
            .add("POST", "/signup/user", dispatcher_sign_up::userRegistration)
            .add("POST", "/signup/student", dispatcher_sign_up::studentRegistration)
            .add("POST", "/sign_in", dispatcher_sign_in::userAuthorization)
}