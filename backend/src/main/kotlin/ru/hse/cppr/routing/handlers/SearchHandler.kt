package ru.hse.cppr.routing.handlers

import arrow.syntax.function.curried
import arrow.syntax.function.reverse
import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.routing.dispatchers.SearchDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.search.*
import ru.hse.cppr.utils.dispatchHandler
import java.util.*

object SearchHandler : KoinComponent {

    private val searchActivitiesService: SearchActivitiesService                by inject()
    private val searchProjectsService: SearchProjectsService                    by inject()
    private val searchProjectRequestsService: SearchProjectRequestsService      by inject()
    private val searchStudentsService: SearchStudentsService                    by inject()
    private val searchQuestionnairesService: SearchQuestionnairesService        by inject()
    private val searchOrganisationsService: SearchOrganisationsService          by inject()
    private val searchUsersService: SearchUsersService                          by inject()

    private val searchActivitiesDispatcher = SearchDispatcher(searchActivitiesService)
    private val searchProjectsDispatcher = SearchDispatcher(searchProjectsService)
    private val searchProjectRequestsDispatcher =
        SearchDispatcher(searchProjectRequestsService)
    private val searchStudentsDispatcher = SearchDispatcher(searchStudentsService)
    private val searchQuestionnairesDispatcher =
        SearchDispatcher(searchQuestionnairesService)
    private val searchOrganisationsDispatcher =
        SearchDispatcher(searchOrganisationsService)

    private val searchUsersDispatcher =
        SearchDispatcher(searchUsersService)


    fun dispatch() =
        Handlers.routing()

            //Search routes
            .add("GET", "/search/activities", AuthorizationProvider.sessionWrapper(searchActivitiesDispatcher::search, ALL_USERS))
            .add("GET", "/search/projects", AuthorizationProvider.sessionWrapper(searchProjectsDispatcher::search, ALL_USERS))
            .add("GET", "/search/project_requests", AuthorizationProvider.sessionWrapper(searchProjectRequestsDispatcher::search, ALL_USERS))
            .add("GET", "/search/students", AuthorizationProvider.sessionWrapper(searchStudentsDispatcher::search, ALL_USERS))
            .add("GET", "/search/organisations", AuthorizationProvider.sessionWrapper(searchOrganisationsDispatcher::search, ALL_USERS))
            .add("GET", "/search/questionnaires/{schemaId}", AuthorizationProvider.sessionWrapper(searchQuestionnairesDispatcher::search, ALL_USERS))
            .add("GET", "/search/mentors", callWrapper(searchUsersDispatcher::search, UserRoles.MENTOR.value))
            .add("GET", "/search/managers", callWrapper(searchUsersDispatcher::search,  UserRoles.MANAGER.value))
            .add("GET", "/search/representatives", callWrapper(searchUsersDispatcher::search, UserRoles.REPRESENTATIVE.value))


    private fun callWrapper(f: (HttpServerExchange) -> Unit, value: String): HttpHandler {
        return HttpHandler({ exchange: HttpServerExchange, next: HttpHandler ->
            addRoleParamWrapper(
                exchange,
                next,
                value
            )

            dispatchHandler(exchange, next)
        }.reverse().curried()(HttpHandler(f)))
    }

    private fun addRoleParamWrapper(exchange: HttpServerExchange, next: HttpHandler, value: String) {
        val paramValue = LinkedList<String>()
        paramValue.add(value)
        exchange.queryParameters.putIfAbsent("role", paramValue)
    }
}