package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.FileDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.file.FileService

object FileHandler : KoinComponent{

    private val fileService: FileService by inject()

    private val fileDispatcher = FileDispatcher(fileService)

    fun dispatch() =
        Handlers.routing()

            // File routes
            .add("OPTIONS", "/upload", fileDispatcher::options)
            .add("OPTIONS", "/file/{id}", fileDispatcher::options)

            .add("POST", "/upload", AuthorizationProvider.sessionWrapper(fileDispatcher::acceptFile, ALL_USERS))
            .add("GET", "/file/{id}", AuthorizationProvider.sessionWrapper(fileDispatcher::downloadFile, ALL_USERS))
            .add("GET", "/files/profile", AuthorizationProvider.sessionWrapper(fileDispatcher::getProfileFiles, ALL_USERS))
            .add("GET", "/files/task", AuthorizationProvider.sessionWrapper(fileDispatcher::getTaskFile, ALL_USERS))
            .add("DELETE", "/file/{id}", AuthorizationProvider.sessionWrapper(fileDispatcher::deleteFile, ALL_USERS))
}