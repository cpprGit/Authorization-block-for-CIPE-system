package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.StudentGroupsDispatcher
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.studentgroups.StudentGroupsService

object StudentGroupsHandler : KoinComponent {

    private val studentGroupsService: StudentGroupsService by inject()

    private val studentGroupsDispatcher = StudentGroupsDispatcher(studentGroupsService)

    fun dispatch() =
        Handlers.routing()
            .add("GET", "/utils/student-groups", AuthorizationProvider.sessionWrapper(studentGroupsDispatcher::getStudentGroups, CPPW))
            .add("POST", "/utils/student-group/update", AuthorizationProvider.sessionWrapper(studentGroupsDispatcher::updateStudentGroups, CPPW))
}