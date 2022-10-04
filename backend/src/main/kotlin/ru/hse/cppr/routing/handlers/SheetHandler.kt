package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.SheetDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.sheets.SheetService

object SheetHandler : KoinComponent{

    private val sheetService: SheetService by inject()

    private val sheetDispatcher = SheetDispatcher(sheetService)

    fun dispatch() =
        Handlers.routing()

            // Sheet routes
            .add("GET", "/sheet/activity/{id}", AuthorizationProvider.sessionWrapper(sheetDispatcher::getActivitySheet, ALL_USERS))
            .add("GET", "/sheet/project/{id}", AuthorizationProvider.sessionWrapper(sheetDispatcher::getProjectSheet, ALL_USERS))
            .add("POST", "/sheet/project/update", AuthorizationProvider.sessionWrapper(sheetDispatcher::updateGrade, ALL_USERS))
}