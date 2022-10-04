package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.ActivitiesService

object FormattedStudentProjectApplyHandler : KoinComponent{


    fun dispatch() =
        Handlers.routing()

            .add("GET", "/formatted/projects/student/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getStudentActiveProjects, ALL_USERS))
            .add("GET", "/formatted/student/applied/projects/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getStudentAppliedProjects, ALL_USERS))
            .add("GET", "/formatted/project/applied/students/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getProjectAppliedStudents, ALL_USERS))
            .add("GET", "/formatted/project/students/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getProjectStudents, ALL_USERS))
            .add("GET", "/formatted/projects/mentor/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getMentorsProjects, ALL_USERS))

}