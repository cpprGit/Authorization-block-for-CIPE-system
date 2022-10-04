package ru.hse.cppr.routing.dispatchers

import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.provider.AuthorizationProvider
import ru.hse.cppr.representation.enums.fields.FileFields
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.service.file.FileService
import ru.hse.cppr.utils.formOkResponse
import ru.hse.cppr.utils.header
import ru.hse.cppr.utils.status
import java.io.*
import java.nio.ByteBuffer
import java.util.*


class FileDispatcher(val service: FileService): KoinComponent {
    private val log: Log                                        by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    fun options(exchange: HttpServerExchange): Unit {
        formOkResponse(exchange, "Method allowed")
    }

    fun acceptFile(exchange: HttpServerExchange): Unit {
        val profileId = exchange.queryParameters["profileId"]?.first()?.let(UUID::fromString)
        val taskId = exchange.queryParameters["taskId"]?.first()?.let(UUID::fromString)

        val uploaderId =
            UUID.fromString(AuthorizationProvider.getDecodedJwt(exchange).getClaim(JwtClaims.ID.value).asString())

        exchange.requestReceiver.receiveFullBytes { _: HttpServerExchange?, message: ByteArray? ->
            log.i("Method: ${service.serviceName} - Upload File")
            message?.let { formOkResponse(exchange, service.saveFile(it, taskId, uploaderId)) }
            log.i("Method: ${service.serviceName} - Upload File - Success")
        }
    }

    fun downloadFile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        val fileInfo = service.downloadFile(id)

        if (fileInfo != null) {

            val file = File(fileInfo[FileFields.LOCATION.value].toString())
            val byteArray = file.readBytes()
            val byteBuffer = ByteBuffer.wrap(byteArray)

            exchange.status(StatusCodes.OK)
            exchange.header("Access-Control-Allow-Origin", "*")
            exchange.header("Content-Type", fileInfo[FileFields.MIMETYPE.value].toString())
            exchange.header(
                "Content-Disposition",
                "attachment; filename=\"${fileInfo[FileFields.NAME.value]}\""
            )
            exchange.responseSender.send(byteBuffer)
        }
    }

    fun getProfileFiles(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get files for profile with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getFilesForProfile(id))))
        log.i("Method: ${service.serviceName} - Get profile files success")
    }

    fun getTaskFile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get file for task with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.getFileForTask(id))))
        log.i("Method: ${service.serviceName} - Get task file success")
    }

    fun deleteFile(exchange: HttpServerExchange): Unit {
        val id = exchange.queryParameters["id"]!!.first().let(UUID::fromString)

        log.i("Method: ${service.serviceName} - Get file for task with id=$id")
        formOkResponse(exchange, JsonStream.serialize(Any.wrap(service.deleteFile(id))))
        log.i("Method: ${service.serviceName} - Get task file success")
    }

}

