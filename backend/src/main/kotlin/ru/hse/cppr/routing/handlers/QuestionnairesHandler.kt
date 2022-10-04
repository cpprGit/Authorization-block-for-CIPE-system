package ru.hse.cppr.routing.handlers

import io.undertow.Handlers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.routing.MainHandler
import ru.hse.cppr.routing.QuestionnairesDispatcher
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.service.crud.ActivitiesService
import ru.hse.cppr.service.profile.QuestionnaireProfileService

object QuestionnairesHandler : KoinComponent{

    private val questionnaireProfileService: QuestionnaireProfileService        by inject()

    private val questionnaireProfileDispatcher = ProfileDispatcher(questionnaireProfileService)



    fun dispatch() =
        Handlers.routing()

            //Questionnaires routes
            .add("POST", "/questionnaires/", AuthorizationProvider.sessionWrapper(QuestionnairesDispatcher::createQuestionnaires, CPPW))
            .add("GET", "/questionnaires/{id}", AuthorizationProvider.sessionWrapper(QuestionnairesDispatcher::getQuestionnairesForUser, ALL_USERS))
            .add("GET", "/questionnaires/", AuthorizationProvider.sessionWrapper(QuestionnairesDispatcher::getQuestionnaires, ALL_USERS))
            .add("GET", "/formatted/questionnaire-profile/{id}", AuthorizationProvider.sessionWrapper(questionnaireProfileDispatcher::getProfile, ALL_USERS))
            .add("POST", "/questionnaires/{id}", AuthorizationProvider.sessionWrapper(questionnaireProfileDispatcher::editProfile, CPPW))


}