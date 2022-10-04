package ru.hse.cppr.routing

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.data.database_generated.enums.ActivityStatus
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.DefaultAttributesService
import ru.hse.cppr.service.FormattedService
import ru.hse.cppr.service.SearchUtils
import ru.hse.cppr.service.crud.formatted.FormattedMailGroupService
import ru.hse.cppr.utils.*
import java.util.*


object FormattedDispatcher: KoinComponent {

    private val log: Log                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val formattedMailGroupService: FormattedMailGroupService    by inject()

    fun startNewAcademicYear(exchange: HttpServerExchange): Unit {

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.startNewAcademicYear()))
        )
    }


    fun addStudentToActivity(exchange: HttpServerExchange): Unit {
        val studentId = exchange.queryParameters["studentId"]!!.first().let(UUID::fromString)
        val activityId = exchange.queryParameters["activityName"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.addStudentToActivity(activityId, studentId)))
        )
    }


    fun getMentorActivities(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getMentorActivities(id)))
        )
    }

    fun getMentorStudents(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getMentorStudents(id)))
        )
    }

    fun getStudentMentors(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getStudentMentors(id)))
        )
    }

    fun getComplaints(exchange: HttpServerExchange): Unit {
        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getComplaintsList()))
        )
    }

    fun sendComplaint(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)
            val newBody = bodyWithCreatedByField(body, AuthorizationProvider.getDecodedJwt(exchange))

            formOkResponse(
                exchange,
                JsonStream.serialize(Any.wrap(FormattedService.createComplaint(newBody))
                )
            )
        }
    }


    fun setComplaintViewed(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val isBlocked = exchange.queryParameters["viewed"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setComplaintViewed(id, isBlocked))
            )
        )
    }

    fun setBlockOrganisation(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val isBlocked = exchange.queryParameters["block"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setOrganisationBlocked(id, isBlocked))
            )
        )
    }


    fun sendNotificationToMailGroup(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)
            val newBody = bodyWithCreatedByField(body, AuthorizationProvider.getDecodedJwt(exchange))


            formOkResponse(
                exchange,
                JsonStream.serialize(Any.wrap(FormattedService.sendTextNotificationToMailGroup(id, newBody))
                )
            )
        }
    }

    fun setUserBlock(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val isBlocked = exchange.queryParameters["block"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setUserBlocked(id, isBlocked))
            )
        )
    }


    fun setProjectRequestStatus(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val status = exchange.queryParameters["status"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setProjectRequestStatus(id, status))
            )
        )
    }


    fun setStudentStatus(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val status = exchange.queryParameters["status"]!!.first()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setStudentStatus(id, status))
            )
        )
    }

    fun getOrganisationEmployers(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getOrganisationEmployers(id)))
        )
    }

    fun getArchivedSchemas(exchange: HttpServerExchange): Unit {
        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.getArchivedSchemas()))
        )
    }

    fun setSchemaArchived(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val archived = exchange.queryParameters["archived"]!!.first().toString()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.setSchemaArchived(id, archived.toBoolean())))
        )
    }

    fun openCloseProjectApplyPeriod(exchange: HttpServerExchange): Unit {
        val name = exchange.queryParameters["name"]!!.first().toString()
        val status = exchange.queryParameters["status"]!!.first().toString()

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(FormattedService.openCloseProjectApplyPeriod(name, status.toBoolean())))
        )
    }

    fun setActivityStatus(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)


            formOkResponse(
                exchange,
                JsonStream.serialize(Any.wrap(FormattedService.setActivityStatus(id, body)))
            )
        }
    }


    fun getNotFinishedActivities(exchange: HttpServerExchange): Unit {
        log.i("GET: Not finished activities")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getAllNotFinishedActivities())))
    }

    fun getActivityProjectsByStatus(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val status = ActivityStatus.valueOf(exchange.queryParameters["status"]!!.first().toString())
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getActivityProjectsByStatus(id, status))))
    }

    fun getActivityProjects(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        val userId = exchange.queryParameters["userId"]?.firstOrNull()?.let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getAllActivityProjectsForApply(id, userId))))
    }

    fun getProjectStudents(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getProjectStudents(id))))
    }

    fun getProjectAppliedStudents(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getProjectAppliedStudents(id))))
    }

    fun getStudentAppliedProjects(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getStudentAppliedProjects(id))))
    }

    fun getStudentActiveProjects(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getStudentActiveProjects(id))))
    }


    fun getStudentActivities(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getStudentActivities(id))))
    }


    fun getMentorsProjects(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getAllMentorsProjects(id))))
    }

    fun getMentorsProjectRequests(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getAllMentorsProjectRequests(id))))
    }

    fun getAllUsersForMailGroup(exchange: HttpServerExchange): Unit {
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.getAllUsersForMailGroups())))
        log.i("Method: FormattedDispatcher LIST - Success")
    }

    fun acceptProjectRequest(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.acceptProjectRequest(id))))
    }

    fun rejectProjectRequest(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(FormattedService.rejectProjectRequest(id))))
    }


    fun getMailGroupsForUser(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: FormattedDispatcher LIST - id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(formattedMailGroupService.listByUserId(id))))
        log.i("Method: FormattedDispatcher LIST - Success")
    }



    fun getAttributesBySchemaType(exchange: HttpServerExchange): Unit {
        val schemaType = exchange.queryParameters["schema_type"]!!.firstOrNull()

        formOkResponse(
            exchange, JsonStream.serialize(
                Any.wrap(
                    DefaultAttributesService.getDefaultAttributesForSchemaType(
                        schemaType.toString()
                    )
                )
            )
        )
    }

    fun getFieldsBySchemaType(exchange: HttpServerExchange): Unit {
        val schemaType = exchange.queryParameters["schema_type"]!!.firstOrNull()

        formOkResponse(
            exchange, JsonStream.serialize(
                Any.wrap(
                    DefaultAttributesService.getDefaultFieldsForSchemaType(
                        schemaType.toString()
                    )
                )
            )
        )
    }


    fun getAttributesByUsageName(exchange: HttpServerExchange): Unit {
        val name = exchange.queryParameters["name"]!!.first()

        log.i("Method: FormattedDispatcher GET - usage_name=$name")
        formOkResponse(exchange, JsonStream.serialize(FormattedService.listAttributesByUsageName(name)))
        log.i("Method: FormattedDispatcher GET - Success")
    }


    fun searchOrganisationsByName(exchange: HttpServerExchange): Unit {
        val name = exchange.queryParameters["name"]?.first()

        formOkResponse(
            exchange, JsonStream.serialize(
                SearchUtils.searchOrganisationsByName(name)
            )
        )

    }

    fun searchMentorsByName(exchange: HttpServerExchange): Unit {
        val mentorName = exchange.queryParameters["name"]?.first()

        formOkResponse(
            exchange, JsonStream.serialize(
                SearchUtils.searchMentorsByName(mentorName)
            )
        )

    }

}