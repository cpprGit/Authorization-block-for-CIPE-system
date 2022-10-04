package ru.hse.cppr.dependencies

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.typeclasses.Concurrent
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import ru.hse.cppr.utils.PropertyKey
import org.koin.dsl.module.module
import ru.hse.cppr.application.AppConfig.getDatabasePassword
import ru.hse.cppr.application.AppConfig.getDatabasePoolCore
import ru.hse.cppr.application.AppConfig.getDatabasePoolMax
import ru.hse.cppr.application.AppConfig.getDatabaseUri
import ru.hse.cppr.application.AppConfig.getDatabaseUsername
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.crud.*
import ru.hse.cppr.service.crud.formatted.*
import ru.hse.cppr.service.file.FileService
import ru.hse.cppr.service.file.FileServiceImpl
import ru.hse.cppr.service.grades.StudentGradesService
import ru.hse.cppr.service.grades.StudentGradesServiceImpl
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryService
import ru.hse.cppr.service.history.mailgroup.MailGroupHistoryServiceImpl
import ru.hse.cppr.service.notifications.NotificationsService
import ru.hse.cppr.service.notifications.NotificationsServiceImpl
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsServiceImpl
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.service.posts.PostsServiceImpl
import ru.hse.cppr.service.profile.*
import ru.hse.cppr.service.report.*
import ru.hse.cppr.service.search.*
import ru.hse.cppr.service.sheets.SheetService
import ru.hse.cppr.service.sheets.SheetServiceImpl
import ru.hse.cppr.service.student.FormattedStudentProjectApplyService
import ru.hse.cppr.service.studentgroups.StudentGroupsService
import ru.hse.cppr.service.studentgroups.StudentGroupsServiceImpl
import ru.hse.cppr.service.users.UsersService
import ru.hse.cppr.service.users.UsersServiceImpl
import ru.hse.cppr.utils.PropertyValue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.reflect.KClass

val effectsModule    = module {
    single<Concurrent<ForIO>> { IO.concurrent() }
}

val cachesModule      = module {
    single<ConcurrentHashMap<PropertyKey, PropertyValue>> { ConcurrentHashMap() }
}

val formsService = module {
    single { SchemaContentService("FormsService") }
}

val schemaService = module {
    single { SchemaService("SchemaService") }
}

val formattedSchemaService = module {
    single { FormattedSchemaService("FormattedSchemaService") }
}

val formattedFormService = module {
    single { FormattedSchemaContentService("FormattedFormService") }
}

val attributesDictionaryService = module {
    single<AttributesDictionaryService> { AttributesDictionaryService("AttributesService") }
}

val projectsService = module {
    single<ProjectService> { ProjectService("ProjectService") }
}

val projectRequestService = module {
    single<ProjectRequestService> { ProjectRequestService("ProjectRequestService") }
}

val formattedProjectRequestService = module {
    single<FormattedProjectRequestService> { FormattedProjectRequestService("FormattedProjectRequestService") }
}

val tasksService = module {
    single<TasksService> { TasksService("TasksService") }
}

val stagesService = module {
    single<StagesService> { StagesService("StagesService") }
}

val activitiesService = module {
    single<ActivitiesService> { ActivitiesService("ActivitiesService") }
}

val mailGroupsService = module {
    single<MailGroupsService> { MailGroupsService("MailGroupsService") }
}

val formattedMailGroupsService = module {
    single<FormattedMailGroupService> { FormattedMailGroupService("MailGroupsService") }
}

val formattedProjectsService = module {
    single<FormattedProjectService> {
        FormattedProjectService(
            "FormattedProjectService"
        )
    }
}

val formattedActivitiesService = module {
    single<FormattedActivitiesService> {
        FormattedActivitiesService(
            "FormattedActivitiesService"
        )
    }
}

val userProfileService = module {
    single<UserProfileService> { UserProfileService("UserProfileService") }
}

val questionnaireProfileService = module {
    single<QuestionnaireProfileService> { QuestionnaireProfileService("QuestionnaireProfileService") }
}

val searchUsersService = module {
    single<SearchUsersService> { SearchUsersService("SearchUsersService") }
}

val searchActivitiesService = module {
    single<SearchActivitiesService> { SearchActivitiesService("SearchActivitiesService") }
}

val searchProjectsService = module {
    single<SearchProjectsService> { SearchProjectsService("SearchProjectsService") }
}

val searchProjectRequestsService = module {
    single<SearchProjectRequestsService> { SearchProjectRequestsService("SearchProjectRequestsService") }
}

val searchStudentsService = module {
    single<SearchStudentsService> { SearchStudentsService("SearchStudentsService") }
}

val searchOrganisationsService = module {
    single<SearchOrganisationsService> { SearchOrganisationsService("SearchOrganisationsService") }
}

val searchQuestionnairesService = module {
    single<SearchQuestionnairesService> { SearchQuestionnairesService("SearchQuestionnairesService") }
}

val formattedStudentProjectApplyService = module {
    single<FormattedStudentProjectApplyService> { FormattedStudentProjectApplyService("FormattedStudentProjectApplyService") }
}


val organisationsService = module {
    single<OrganisationsService> { OrganisationsService("OrganistaionsService") }
}


val formattedOrganisationsService = module {
    single<FormattedOrganisationsService> { FormattedOrganisationsService("FormattedOrganistaionsService") }
}

val projectsInfoReportService = module {
    single<ProjectsInfoReportService> { ProjectsInfoReportService("ProjectsInfoReportService") }
}

val projectsSearchReportService = module {
    single<ProjectsSearchReportService> { ProjectsSearchReportService("ProjectsSearchReportService") }
}

val profileReportService = module {
    single<ProfileReportService> { ProfileReportService("ProfileReportService") }
}

val organisationService = module {
    single<OrganisationRelationshipsService> { OrganisationRelationshipsServiceImpl("OrganisationService") }
}

val notificationsService = module {
    single<NotificationsService> { NotificationsServiceImpl("NotificationsServiceImpl") }
}

val fileService = module {
    single<FileService> { FileServiceImpl("FileServiceImpl") }
}

val postsService = module {
    single<PostsService> { PostsServiceImpl("PostsServiceImpl") }
}

val mailGroupHistoryService = module {
    single<MailGroupHistoryService> { MailGroupHistoryServiceImpl("MailGroupHistoryServiceImpl") }
}

val studentGradesService = module {
    single<StudentGradesService> { StudentGradesServiceImpl("StudentGradesServiceImpl") }
}

val sheetService = module {
    single<SheetService> { SheetServiceImpl("SheetServiceImpl") }
}


val projectProfileForStudentService = module {
    single<ProjectProfileForStudentService> { ProjectProfileForStudentServiceImpl("ProjectProfileForStudentServiceeImpl") }
}

val activityProfileService = module {
    single<ActivityProfileService> { ActivityProfileService("ActivityProfileService") }
}

val projectProfileService = module {
    single<ProjectProfileService> { ProjectProfileService("ProjectProfileService") }
}

val projectRequestProfileService = module {
    single<ProjectRequestProfileService> { ProjectRequestProfileService("ProjectRequestProfileService") }
}

val organisationProfileService = module {
    single<OrganisationProfileService> { OrganisationProfileService("OrganisationProfileService") }
}

val usersService = module {
    single<UsersService> { UsersServiceImpl("UsersServiceImpl") }
}

val searchReportService = module {
    single<SearchReportService> { SearchReportService ("SearchReportService ") }
}

val studentGroupsService = module {
    single<StudentGroupsService> { StudentGroupsServiceImpl ("StudentGroupsImpl ") }
}



val clientsModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(16, 5, TimeUnit.MINUTES))
            .dispatcher(Dispatcher(Executors.newFixedThreadPool(16, object: ThreadFactory {
                private val counter = AtomicInteger(0)
                override fun newThread(runnable: Runnable): Thread {
                    val thread = Thread(runnable)
                    thread.name = "OkHttpThread[${counter.incrementAndGet()}]"
                    return thread
                }
            })))
//            .certificatePinner(CertificatePinner.Builder()
//                .add("dev-cppr.eu.auth0.com", "sha256/KIc+hhZBDis2gRd2UijVH++K1ZUBEJ/kIHfb427RGX0=")
//                .build()
//            )
            .build()
    }
}

val applicationModule = module {
    scope(ApplicationScope.name) { (propertiesDir: String) ->
        ApplicationScope(propertiesDir)
    }

    factory<Log> { (clazz: KClass<*>) ->
        Log(clazz.simpleName!!)
    }
}

val databaseModule   = module {
    single<DataSource> {
        HikariDataSource(HikariConfig().apply {
            setJdbcUrl         (getDatabaseUri())
            setUsername        (getDatabaseUsername())
            setPassword        (getDatabasePassword())
            setMinimumIdle     (getDatabasePoolCore().toInt())
            setMaximumPoolSize (getDatabasePoolMax().toInt())
        })
    }

    single {
        TxProvider(get<Concurrent<ForIO>>(), get())
    }
}