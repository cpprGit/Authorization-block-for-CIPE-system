package ru.hse.ccpr.application

import io.undertow.util.StatusCodes
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.jupiter.api.Assertions
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.ccpr.utils.*
import ru.hse.cppr.application.ITCommandLineApplicationRuntime
import ru.hse.cppr.logging.Log

abstract class BaseIntegrationTestController: KoinComponent {

    private val log: Log                                                  by inject() {
        parametersOf(
            ITCommandLineApplicationRuntime::class
        )
    }

    private val client: OkHttpClient                                      by inject()

    abstract val path: String


    private var passedTests = 0
    private var failedTests = 0

    abstract fun run(): Int

    fun runTests(vararg tests: () -> (Unit)): Int {
        log.testRun("Running: ${this::class.simpleName}; Total number of tests: ${tests.size}")
        for (test in tests) {
            try {
                test()
                log.testSuccess("Test passed: $test")
                passedTests++
            } catch (e: Exception) {
                log.testFailure("Test failed: $test")
                log.testFailure("Reason: ${e.message}")
                e.printStackTrace()
                failedTests++
            } catch (e: Error) {
                log.testFailure("Test failed: $test")
                log.testFailure("Reason: ${e.message}")
                e.printStackTrace()
                failedTests++
            }
        }

        log.testStatus("Report: ${this::class.simpleName}; Passed: $passedTests; Failed: $failedTests")
        return failedTests
    }

    fun createAndGetResource(createPath: String, savedPath: String) {
        val createResponse =
            runBlocking { postRequest(fromResource(createPath), path) }

        Assertions.assertEquals(StatusCodes.CREATED, createResponse.code)

        val getResponse = runBlocking { getRequest(getIdFromResponse(createResponse), path) }
        Assertions.assertEquals(StatusCodes.OK, getResponse.code)

        compareJsonObjects(
            jsonFromString(fromResource(savedPath)),
            jsonFromString(getResponse.body?.string().toString())
        )
    }

    fun createAndGetResource(postPath: String, getPath: String, createPath: String, savedPath: String) {
        val createResponse =
            runBlocking { postRequest(fromResource(createPath), postPath) }

        Assertions.assertEquals(StatusCodes.CREATED, createResponse.code)

        val getResponse = runBlocking { getRequest(getIdFromResponse(createResponse), getPath) }
        Assertions.assertEquals(StatusCodes.OK, getResponse.code)

        compareJsonObjects(
            jsonFromString(fromResource(savedPath)),
            jsonFromString(getResponse.body?.string().toString())
        )
    }


    protected fun fromResource(pathToResource: String): String {
        return this::class.java.getResource(pathToResource).readText()
    }

    protected fun postRequest(requestBody: String, path: String): Response {
        return client.newCall(formPostRequest(requestBody, path)).execute()
    }

    protected fun getRequest(id: String, path: String): Response {
        return client.newCall(formGetRequest("$path$id")).execute()
    }

}