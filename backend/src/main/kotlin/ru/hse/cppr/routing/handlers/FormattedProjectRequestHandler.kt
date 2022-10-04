package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.crud.formatted.FormattedProjectRequestService
import ru.hse.cppr.service.profile.ProjectProfileService
import ru.hse.cppr.service.profile.ProjectRequestProfileService

object FormattedProjectRequestHandler : KoinComponent{

    private val formattedProjectRequestService: FormattedProjectRequestService  by inject()

    private val projectRequestProfileService: ProjectRequestProfileService      by inject()


    private val formattedProjectRequestDispatcher =
        CRUDDispatcher(formattedProjectRequestService)

    private val profileDispatcher = ProfileDispatcher(projectRequestProfileService)

    fun dispatch() =
        Handlers.routing()

            //Formatted Project Request Routes

            .add("GET", "/formatted/project_requests/mentor/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getMentorsProjectRequests, ALL_USERS))
            .add("POST", "/formatted/project_request/", AuthorizationProvider.sessionWrapper(formattedProjectRequestDispatcher::post, ALL_USERS))
            .add("GET", "/formatted/project_request-profile/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::getProfile, ALL_USERS))
            .add("POST", "/formatted/project_request-profile/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::editProfile, ALL_USERS))
            .add("POST", "/formatted/project_request/accept/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::acceptProjectRequest, ALL_USERS))
            .add("POST", "/formatted/project_request/reject/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::rejectProjectRequest, ALL_USERS))
            .add("POST", "/formatted/project_request/cancel/{id}", AuthorizationProvider.sessionWrapper(formattedProjectRequestDispatcher::delete, ALL_USERS))
            .add("POST", "/formatted/project_request/set-status", AuthorizationProvider.sessionWrapper(FormattedDispatcher::setProjectRequestStatus, ALL_USERS))


}