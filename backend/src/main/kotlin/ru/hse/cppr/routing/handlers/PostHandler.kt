package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.FileDispatcher
import ru.hse.cppr.routing.dispatchers.PostDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.file.FileService
import ru.hse.cppr.service.posts.PostsService

object PostHandler : KoinComponent{

    private val postsService: PostsService by inject()

    private val postDispatcher = PostDispatcher(postsService)

    fun dispatch() =
        Handlers.routing()

            // Posts routes
            .add("POST", "/post/create", AuthorizationProvider.sessionWrapper(postDispatcher::createPost, ALL_USERS))
            .add("GET", "/posts/get", AuthorizationProvider.sessionWrapper(postDispatcher::getPostsForProfile, ALL_USERS))
            .add("POST", "/post/update", AuthorizationProvider.sessionWrapper(postDispatcher::updatePost, ALL_USERS))
            .add("POST", "/post/delete", AuthorizationProvider.sessionWrapper(postDispatcher::deletePost, ALL_USERS))
}