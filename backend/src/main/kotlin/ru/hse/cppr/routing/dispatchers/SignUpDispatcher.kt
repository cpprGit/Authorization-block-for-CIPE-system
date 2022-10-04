package ru.hse.cppr.routing.dispatchers

import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.exception.Auth0Exception
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.service.Auth0Service.createUserInAuth0
import ru.hse.cppr.service.users.UsersService
import ru.hse.cppr.utils.*
import java.util.*


class SignUpDispatcher(val service: UsersService) : KoinComponent {
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }


    fun studentRegistration(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            val savedStudent = service.persistStudent(body)

/*
            try {
                    createUserInAuth0(body, savedStudent[CommonFields.ID.value].toString())
            } catch (e: Exception) {
                service.deleteStudent(UUID.fromString(savedStudent[CommonFields.ID.value].toString()))
                throw Auth0Exception(e.message, e)
            }
*/

            formCreatedResponse(exchange, JsonStream.serialize(com.jsoniter.any.Any.wrap(savedStudent)))
        }
    }

    fun userRegistration(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)

            log.i("Trying to create new user with email=\'${body["email"].`as`<String>()}\'")

            val savedUser = service.persistUser(body)

/*
            try {
                createUserInAuth0(body, savedUser[CommonFields.ID.value].toString())
            } catch (e: Exception) {
                service.deleteUser(UUID.fromString(savedUser[CommonFields.ID.value].toString()))
                throw Auth0Exception(e.message, e)
            }
*/

            formCreatedResponse(exchange, JsonStream.serialize(savedUser))
            log.i("Method: dispatchSignUp POST - User successfully created")
        }
    }
}