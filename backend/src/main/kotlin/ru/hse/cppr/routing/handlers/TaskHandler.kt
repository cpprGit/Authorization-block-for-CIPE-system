package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.routing.dispatchers.SearchDispatcher
import ru.hse.cppr.routing.dispatchers.StudentProjectApplyDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.*
import ru.hse.cppr.service.crud.formatted.*
import ru.hse.cppr.service.profile.QuestionnaireProfileService
import ru.hse.cppr.service.profile.UserProfileService
import ru.hse.cppr.service.search.*
import ru.hse.cppr.service.student.FormattedStudentProjectApplyService

object TaskHandler : KoinComponent{

    private val tasksService: TasksService                                      by inject()

    private val tasksDispatcher = CRUDDispatcher(tasksService)


    fun dispatch() =
        Handlers.routing()

            // Tasks routes
            .add("POST", "/task", AuthorizationProvider.sessionWrapper(tasksDispatcher::post, ALL_USERS))
            .add("POST", "/task/",  AuthorizationProvider.sessionWrapper(tasksDispatcher::post, ALL_USERS))
            .add("GET", "/tasks",  AuthorizationProvider.sessionWrapper(tasksDispatcher::list, ALL_USERS))
            .add("GET", "/tasks/", AuthorizationProvider.sessionWrapper(tasksDispatcher::list, ALL_USERS))
            .add("GET", "/task/{id}",  AuthorizationProvider.sessionWrapper(tasksDispatcher::get, ALL_USERS))
            .add("POST", "/task/{id}", AuthorizationProvider.sessionWrapper(tasksDispatcher::update, ALL_USERS))
            .add("DELETE", "/task/{id}",  AuthorizationProvider.sessionWrapper(tasksDispatcher::delete, ALL_USERS))
}