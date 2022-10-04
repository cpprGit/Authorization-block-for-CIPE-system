package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.ReportDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.report.*

object ReportHandler : KoinComponent {

    //Services
    private val projectsInfoReportService: ProjectsInfoReportService by inject()
    private val profileReportService: ProfileReportService by inject()
    private val projectsSearchReportService: ProjectsSearchReportService by inject()
    private val searchReportService: SearchReportService by inject()

    //Dispatcher instances
    private val projectsInfoReportDispatcher =
        ReportDispatcher(projectsInfoReportService)
    private val profileReportDispatcher =
        ReportDispatcher(profileReportService)
    private val projectsSearchReportDispatcher =
        ReportDispatcher(projectsSearchReportService)

    private val searchReportDispatcher =
            ReportDispatcher(searchReportService)


    fun dispatch() =
        Handlers.routing()
            .add("GET", "/reports/projects-info", AuthorizationProvider.sessionWrapper(projectsInfoReportDispatcher::get, ALL_USERS))
            .add("GET", "/reports/profile", AuthorizationProvider.sessionWrapper(profileReportDispatcher::get, CPPW))
            .add("GET", "/reports/projects-search", AuthorizationProvider.sessionWrapper(projectsSearchReportDispatcher::get, ALL_USERS))
            .add("GET", "/reports/search", AuthorizationProvider.sessionWrapper(searchReportDispatcher::get, ALL_USERS))

}