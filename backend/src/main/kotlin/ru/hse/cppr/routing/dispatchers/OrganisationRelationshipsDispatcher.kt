package ru.hse.cppr.routing.dispatchers

import arrow.core.Either
import arrow.fx.fix
import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.crud.CRUDService
import ru.hse.cppr.service.organisation.OrganisationRelationshipsService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.formCreatedResponse
import ru.hse.cppr.utils.formOkResponse
import java.util.*

class OrganisationRelationshipsDispatcher(val service: OrganisationRelationshipsService): KoinComponent {

    private val log: Log                by inject() { parametersOf(CommandLineApplicationRuntime::class) }


    fun getOrganisationDescendants(exchange: HttpServerExchange): Unit {
        val id = when (val i = exchange.queryParameters["id"]?.first()) {
            "" -> null
            else -> i.let(UUID::fromString)
        }

        log.i("Method: ${service.serviceName} - Get descendants of id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getDescendants(id))))
        log.i("Method: ${service.serviceName} - GET - Success")

    }


    fun getOrganisationAncestors(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getAncestors(id))))
    }

}