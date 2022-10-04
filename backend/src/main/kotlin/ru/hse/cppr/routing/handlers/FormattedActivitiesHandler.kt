package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.crud.formatted.FormattedActivitiesService
import ru.hse.cppr.service.profile.ActivityProfileService

object FormattedActivitiesHandler : KoinComponent{

    private val formattedActivitiesService: FormattedActivitiesService          by inject()
    private val activityProfileService: ActivityProfileService                  by inject()

    private val formattedActivitiesDispatcher =
        CRUDDispatcher(formattedActivitiesService)

    private val profileDispatcher = ProfileDispatcher(activityProfileService)

    fun dispatch() =
        Handlers.routing()
            //Formatted activities routes

            .add("GET", "/formatted/activities/not-finished", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getNotFinishedActivities, ALL_USERS))
            .add("GET", "/formatted/activities/student/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getStudentActivities, ALL_USERS))
            .add("POST", "/formatted/activity/set-status/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::setActivityStatus, CPPW))
            .add("POST", "/formatted/activity/", AuthorizationProvider.sessionWrapper(formattedActivitiesDispatcher::post, CPPW))

            .add("POST", "/formatted/activity/update/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::editProfile, CPPW))
            .add("GET", "/formatted/activity-profile/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::getProfile, CPPW))

            .add("GET", "/formatted/projects/activity/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getActivityProjects, CPPW))
            .add("GET", "/formatted/projects/with-status/activity/{id}", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getActivityProjectsByStatus, CPPW))

}