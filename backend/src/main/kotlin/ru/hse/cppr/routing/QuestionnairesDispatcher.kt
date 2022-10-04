package ru.hse.cppr.routing

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.service.CurrentSchemaService
import ru.hse.cppr.service.QuestionnairesService
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.bodyWithCreatedByField
import ru.hse.cppr.utils.formOkResponse
import java.util.*

object QuestionnairesDispatcher : KoinComponent {
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }


    fun createQuestionnaires(exchange: HttpServerExchange): Unit {
        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            val text = String(message!!, charset("utf-8"))
            val body = JsonIterator.deserialize(text)
            val newBody = bodyWithCreatedByField(body, AuthorizationProvider.getDecodedJwt(exchange))

            formOkResponse(
                exchange,
                JsonStream.serialize(Any.wrap(QuestionnairesService.createQuestionnairesFromMailGroup(newBody)))
            )
        }

    }

    fun getQuestionnairesForUser(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(QuestionnairesService.getQuestionnairesByUserId(id)))
        )
    }

    fun getQuestionnaires(exchange: HttpServerExchange): Unit {
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(QuestionnairesService.getAllQuestionnairesForms())))
    }

    fun getQuestionnaireProfile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        formOkResponse(
            exchange,
            JsonStream.serialize(Any.wrap(QuestionnairesService.getQuestionnaireProfile(id)))
        )
    }

}