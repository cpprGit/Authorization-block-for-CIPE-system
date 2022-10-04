package ru.hse.cppr.application

import arrow.core.Either
import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.UndertowOptions
import io.undertow.server.handlers.PathHandler
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig.getServerHost
import ru.hse.cppr.application.AppConfig.getServerPort
import ru.hse.cppr.application.AppConfig.getServerThreadsCore
import ru.hse.cppr.application.AppConfig.getServerThreadsIo
import ru.hse.cppr.routing.*
import ru.hse.cppr.logging.Log

class CommandLineApplicationRuntime: KoinComponent {

    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    fun run() {
        val undertow = Undertow.builder()
            .addHttpListener(getServerPort(), getServerHost(), createPathHandler())
            .setIoThreads(getServerThreadsIo())
            .setWorkerThreads(getServerThreadsCore())
            .setServerOption(UndertowOptions.URL_CHARSET, "UTF8")
            .build()

        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                Either.catch { undertow.stop() }
            }
        })

        undertow.start()

        log.i("Application started")
    }

    private fun createPathHandler(): PathHandler? {
        return Handlers.path()
            .addPrefixPath("/api/v1", MainHandler.dispatch())

            .addPrefixPath("/", MainHandler.staticResource())
            .addPrefixPath("/home", MainHandler.staticResource())
            .addPrefixPath("/profile", MainHandler.staticResource())
            .addPrefixPath("/chat", MainHandler.staticResource())
            .addPrefixPath("/help", MainHandler.staticResource())
            .addPrefixPath("/search", MainHandler.staticResource())
            .addPrefixPath("/forms", MainHandler.staticResource())
            .addPrefixPath("/for_partners", MainHandler.staticResource())
            .addPrefixPath("/for_students", MainHandler.staticResource())
            .addPrefixPath("/authorization", MainHandler.staticResource())
            .addPrefixPath("/for_workers", MainHandler.staticResource())
            .addPrefixPath("/contacts", MainHandler.staticResource())
            .addPrefixPath("/admin", MainHandler.staticResource())
            .addPrefixPath("/new_project_request", MainHandler.staticResource())
            .addPrefixPath("/user", MainHandler.staticResource())
            .addPrefixPath("/organisation", MainHandler.staticResource())
            .addPrefixPath("/project", MainHandler.staticResource())
            .addPrefixPath("/project_request", MainHandler.staticResource())
            .addPrefixPath("/activity", MainHandler.staticResource())
    }
}