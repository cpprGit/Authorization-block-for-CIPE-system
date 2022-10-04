package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.FormattedDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.OrganisationRelationshipsDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW_M_R
import ru.hse.cppr.service.FormattedService
import ru.hse.cppr.service.crud.formatted.FormattedOrganisationsService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsServiceImpl
import ru.hse.cppr.service.profile.OrganisationProfileService

object FormattedOrganisationHandler : KoinComponent {

    private val formattedOrganisationsService: FormattedOrganisationsService                     by inject()
    private val organisationRelationshipsService: OrganisationRelationshipsService               by inject()
    private val organisationProfileService: OrganisationProfileService                           by inject()

    private val organisationRelationshipsDispatcher =
        OrganisationRelationshipsDispatcher(organisationRelationshipsService)

    private val formattedOrganisationsDispatcher =
        CRUDDispatcher(formattedOrganisationsService)

    private val profileDispatcher = ProfileDispatcher(organisationProfileService)


    fun dispatch() =
        Handlers.routing()

            //Organisation routes
            .add("GET", "/formatted/organisation-profile/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::getProfile, ALL_USERS))
            .add("POST", "/formatted/organisation-profile/{id}", AuthorizationProvider.sessionWrapper(profileDispatcher::editProfile, CPPW_M_R))

            .add("POST", "/formatted/organisation/", AuthorizationProvider.sessionWrapper(formattedOrganisationsDispatcher::post, CPPW_M_R))

            .add("GET", "/formatted/organisation/ancestors/{id}", AuthorizationProvider.sessionWrapper(organisationRelationshipsDispatcher::getOrganisationAncestors, ALL_USERS))
            .add("GET", "/formatted/organisation/descendants/{id}", organisationRelationshipsDispatcher::getOrganisationDescendants)

            .add("GET", "/formatted/organisation/{id}/employers", AuthorizationProvider.sessionWrapper(FormattedDispatcher::getOrganisationEmployers, ALL_USERS))

}