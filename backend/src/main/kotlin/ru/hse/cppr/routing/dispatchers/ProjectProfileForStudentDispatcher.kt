package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.security.UserProfileSecurityProvider
import ru.hse.cppr.service.profile.ProfileService
import ru.hse.cppr.service.profile.ProjectProfileForStudentService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class ProjectProfileForStudentDispatcher(val service: ProjectProfileForStudentService): KoinComponent {

    private val log: Log                    by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    private val securityProvider = UserProfileSecurityProvider()

    fun getProfile(exchange: HttpServerExchange): Unit {
        val projectId = exchange.queryParameters["projectId"]!!.first().let(UUID::fromString)
        val studentId = exchange.queryParameters["studentId"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get profile by id=$projectId")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getProfile(projectId, studentId))))
        log.i("Method: ${service.serviceName} GET - Success")
    }

}