package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.StudentProjectApplyDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.student.FormattedStudentProjectApplyService

object StudentProjectApplyHandler : KoinComponent{

    private val formattedStudentProjectApplyService: FormattedStudentProjectApplyService        by inject()

    private val studentProjectApplyDispatcher =
        StudentProjectApplyDispatcher(formattedStudentProjectApplyService)


    fun dispatch() =
        Handlers.routing()
            .add("POST", "/formatted/student/applied/projects", AuthorizationProvider.sessionWrapper(studentProjectApplyDispatcher::applyToProject, ALL_USERS))
            .add("POST", "/formatted/student/applied/projects/cancel", AuthorizationProvider.sessionWrapper(studentProjectApplyDispatcher::cancelApplyToProject, ALL_USERS))
            .add("POST", "/formatted/student/applied/projects/accept", AuthorizationProvider.sessionWrapper(studentProjectApplyDispatcher::acceptApplyToProject, ALL_USERS))
            .add("POST", "/formatted/student/applied/projects/accept/cancel", AuthorizationProvider.sessionWrapper(studentProjectApplyDispatcher::cancelAcceptApplyToProject, ALL_USERS))
            .add("POST", "/formatted/student/applied/projects/decline", AuthorizationProvider.sessionWrapper(studentProjectApplyDispatcher::declineApplyToProject, ALL_USERS))
}