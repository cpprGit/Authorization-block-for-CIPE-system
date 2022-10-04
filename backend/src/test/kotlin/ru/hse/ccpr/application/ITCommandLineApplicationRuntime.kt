package ru.hse.cppr.application

import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.application.service.formatted.*
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.crud.formatted.FormattedActivitiesService

class ITCommandLineApplicationRuntime: KoinComponent {

    private val log: Log                                                  by inject() {
        parametersOf(
            ITCommandLineApplicationRuntime::class
        )
    }

    fun run() {
        log.testStatus("Integration tests launched")

        var failedTestsNumber = 0

        failedTestsNumber += FormattedActivitiesServiceIT().run()
        failedTestsNumber += FormattedSchemaServiceIT().run()
        failedTestsNumber += AttributesServiceIT().run()
        failedTestsNumber += FormattedProfileIT().run()
        failedTestsNumber += MailGroupsServiceIT().run()

        log.testStatus("TESTS FAILED: $failedTestsNumber")
    }


}