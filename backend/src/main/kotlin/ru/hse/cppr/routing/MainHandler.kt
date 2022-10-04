package ru.hse.cppr.routing

import com.auth0.jwt.exceptions.TokenExpiredException
import com.jsoniter.spi.JsonException
import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.util.BadRequestException
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.routing.dispatchers.CRUDDispatcher
import ru.hse.cppr.routing.dispatchers.ProfileDispatcher
import ru.hse.cppr.exception.Auth0Exception
import ru.hse.cppr.exception.AuthorizationException
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.exception.ForbiddenException
import ru.hse.cppr.exception.handler.ExceptionHandlers
import ru.hse.cppr.provider.AuthorizationProvider.sessionWrapper
import ru.hse.cppr.routing.handlers.*
import ru.hse.cppr.security.SecurityRoles.ALL_USERS
import ru.hse.cppr.security.SecurityRoles.CPPW
import ru.hse.cppr.security.SecurityRoles.SUPERVISOR
import ru.hse.cppr.service.crud.formatted.*
import ru.hse.cppr.service.profile.UserProfileService
import java.lang.IllegalArgumentException
import java.nio.file.Paths

object MainHandler : KoinComponent{

    private val formattedSchemaContentService: FormattedSchemaContentService    by inject()

    private val userProfileService: UserProfileService                          by inject()

    private val formattedFormDispatcher = CRUDDispatcher(formattedSchemaContentService)

    private val userProfileDispatcher = ProfileDispatcher(userProfileService)



    fun dispatch(exchange: HttpServerExchange, handler: HttpHandler): Unit {
        if (exchange.isInIoThread) {
            exchange.dispatch(handler)
            return
        }
    }

    //TODO: add wrapper
    fun dispatch() = Handlers.exceptionHandler(
        Handlers.routing()
            .addAll(PostHandler.dispatch())
            .addAll(FormHandler.dispatch())
            .addAll(FileHandler.dispatch())
            .addAll(TaskHandler.dispatch())
            .addAll(UsersHandler.dispatch())
            .addAll(SheetHandler.dispatch())
            .addAll(StageHandler.dispatch())
            .addAll(SearchHandler.dispatch())
            .addAll(ReportHandler.dispatch())
            .addAll(SchemaHandler.dispatch())
            .addAll(ProjectHandler.dispatch())
            .addAll(ActivityHandler.dispatch())
            .addAll(AttributeHandler.dispatch())
            .addAll(NotificationHandler.dispatch())
            .addAll(QuestionnairesHandler.dispatch())
            .addAll(FormattedSchemaHandler.dispatch())
            .addAll(FormattedProjectHandler.dispatch())
            .addAll(MailGroupHistoryHandler.dispatch())
            .addAll(FormattedMailGroupHandler.dispatch())
            .addAll(FormattedActivitiesHandler.dispatch())
            .addAll(StudentProjectApplyHandler.dispatch())
            .addAll(FormattedOrganisationHandler.dispatch())
            .addAll(FormattedProjectRequestHandler.dispatch())
            .addAll(FormattedStudentProjectApplyHandler.dispatch())
            .addAll(StudentGroupsHandler.dispatch())


            .add("GET", "/utils/mentor/{id}/activities", sessionWrapper(FormattedDispatcher::getMentorActivities, ALL_USERS))
            .add("POST", "/utils/activity/add-student", sessionWrapper(FormattedDispatcher::addStudentToActivity, CPPW))
            .add("POST", "/utils/start-academic-year", sessionWrapper(FormattedDispatcher::startNewAcademicYear, CPPW))

            .add("POST", "/utils/block-organisation", sessionWrapper(FormattedDispatcher::setBlockOrganisation, SUPERVISOR))
            .add("POST", "/utils/block-user", sessionWrapper(FormattedDispatcher::setUserBlock, SUPERVISOR))
            .add("GET", "/utils/mentors", sessionWrapper(FormattedDispatcher::searchMentorsByName, ALL_USERS))
            .add("GET", "/utils/organisations", sessionWrapper(FormattedDispatcher::searchOrganisationsByName, ALL_USERS))
            .add("POST", "/utils/set-student-status", sessionWrapper(FormattedDispatcher::setStudentStatus, CPPW))
            .add("GET", "/utils/mentor/students", sessionWrapper(FormattedDispatcher::getMentorStudents, ALL_USERS))
            .add("GET", "/utils/student/mentors", sessionWrapper(FormattedDispatcher::getStudentMentors, ALL_USERS))

            .add("GET", "/complaints", sessionWrapper(FormattedDispatcher::getComplaints, ALL_USERS))
            .add("POST", "/complaint/send", sessionWrapper(FormattedDispatcher::sendComplaint, ALL_USERS))
            .add("POST", "/complaint/set-viewed", sessionWrapper(FormattedDispatcher::setComplaintViewed, ALL_USERS))

            //Formatted routes
            .add("GET", "/formatted/user-profile/{id}", sessionWrapper(userProfileDispatcher::getProfile, ALL_USERS))
            .add("POST", "/formatted/user-profile/{id}", sessionWrapper(userProfileDispatcher::editProfile, ALL_USERS))

            .add("GET", "/formatted/attributes-by-usage-name/{name}", sessionWrapper(FormattedDispatcher::getAttributesByUsageName, ALL_USERS))
            .add("GET", "/formatted/default-attributes/{schema_type}", sessionWrapper(FormattedDispatcher::getAttributesBySchemaType, ALL_USERS))
            .add("GET", "/formatted/default-fields/{schema_type}", sessionWrapper(FormattedDispatcher::getFieldsBySchemaType, ALL_USERS))

            //Formatted Form routes
            .add("GET", "/formatted/schema-content/{id}", sessionWrapper(formattedFormDispatcher::get, ALL_USERS))

            //Default Form routes
            .add("POST", "/current-schema/update/{type}", sessionWrapper(CurrentSchemaDispatcher::patch, ALL_USERS))
            .add("GET", "/current-schema/{type}", sessionWrapper(CurrentSchemaDispatcher::get, ALL_USERS))
            .add("GET", "/formatted/current-schema/{type}", CurrentSchemaDispatcher::getFormatted)

            // Schema attribute routes
            .add("POST", "/attribute-schema/", sessionWrapper(SchemaAttributeDispatcher::addAttributeToSchemaPost, ALL_USERS))
            .add("DELETE", "/attribute/{attr_id}/delete-from-schema/{schema_id}", sessionWrapper(SchemaAttributeDispatcher::removeAttribureFromSchemaDelete, ALL_USERS))

            .add("GET", "/auth-example", sessionWrapper(HomeDispatcher::get, ALL_USERS))
            .add("POST", "/login", LoginDispatcher::post)
            .add("GET", "/", HomeDispatcher::get)
            .add("GET", "/home", HomeDispatcher::get)
            .add("GET", "/callback", CallbackDispatcher::get)
            .add("GET", "/error", ErrorDispatcher::get)!!
    )

        .addExceptionHandler(BadRequestException::class.java, ExceptionHandlers::BadRequestExceptionHandler)
        .addExceptionHandler(DatabaseException::class.java, ExceptionHandlers::BadRequestExceptionHandler)
        .addExceptionHandler(IllegalArgumentException::class.java, ExceptionHandlers::BadRequestExceptionHandler)
        .addExceptionHandler(JsonException::class.java, ExceptionHandlers::BadRequestExceptionHandler)
        .addExceptionHandler(Auth0Exception::class.java, ExceptionHandlers::Auth0ExceptionHandler)
        .addExceptionHandler(AuthorizationException::class.java, ExceptionHandlers::AuthorizationExceptionHandler)
        .addExceptionHandler(ForbiddenException::class.java, ExceptionHandlers::ForbiddenExceptionHandler)
        .addExceptionHandler(TokenExpiredException::class.java, ExceptionHandlers::ForbiddenExceptionHandler)


    //TODO: Probably should forbid access to all files except needed for frontend app
    fun staticResource() = Handlers.resource(PathResourceManager(Paths.get("frontend/build"), 100))!!

}