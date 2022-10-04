package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.routing.dispatchers.ProjectProfileForStudentDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.security.SecurityRoles.CPPW_M_R
import ru.hse.cppr.service.crud.formatted.FormattedProjectService
import ru.hse.cppr.service.profile.ProjectProfileForStudentService
import ru.hse.cppr.service.profile.ProjectProfileService

object FormattedProjectHandler : KoinComponent {

    private val formattedProjectService: FormattedProjectService                        by inject()
    private val projectProfileForStudentService: ProjectProfileForStudentService        by inject()
    private val projectProfileService: ProjectProfileService                            by inject()

    private val formattedProjectDispatcher = CRUDDispatcher(formattedProjectService)

    private val projectProfileForStudentDispatcher = ProjectProfileForStudentDispatcher(projectProfileForStudentService)
    private val projectProfileDispatcher = ProfileDispatcher(projectProfileService)

    fun dispatch() =
        Handlers.routing()

            .add("GET", "/formatted/project-profile/{id}", AuthorizationProvider.sessionWrapper(projectProfileDispatcher::getProfile, ALL_USERS))
            .add("POST", "/formatted/project/update/{id}", AuthorizationProvider.sessionWrapper(projectProfileDispatcher::editProfile, CPPW_M_R))
            .add("GET", "/formatted/project-for-student", AuthorizationProvider.sessionWrapper(projectProfileForStudentDispatcher::getProfile, ALL_USERS))

            .add("POST", "/formatted/project-profile/{id}", AuthorizationProvider.sessionWrapper(formattedProjectDispatcher::update, CPPW_M_R))
            .add("POST", "/formatted/project/", AuthorizationProvider.sessionWrapper(formattedProjectDispatcher::post, CPPW))
            .add("POST", "/formatted/project-apply-period", AuthorizationProvider.sessionWrapper(FormattedDispatcher::openCloseProjectApplyPeriod, CPPW))
}