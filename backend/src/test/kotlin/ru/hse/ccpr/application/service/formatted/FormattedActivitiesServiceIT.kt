package ru.hse.ccpr.application.service.formatted

import io.undertow.util.StatusCodes
import org.junit.jupiter.api.Assertions.*
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.utils.*

class FormattedActivitiesServiceIT: BaseIntegrationTestController() {

    override val path = "/formatted/activity/"
    val postPath = "/formatted/activity/"
    val getPath = "/formatted/activity-profile/"

    override fun run(): Int {
        return runTests(
            this::testActivityIsSavedCorrectly,
            this::testActivityIsSavedCorrectlyRequiredFieldsOnly
        )
    }


    private fun testActivityIsSavedCorrectly() {
        createAndGetResource(postPath, getPath,
            "/requests/formatted/activities/create-formatted-activity.json",
            "/saved/formatted/activities/get-formatted-activity.json"
        )
    }

    private fun testActivityIsSavedCorrectlyRequiredFieldsOnly() {
        createAndGetResource(postPath, getPath,
            "/requests/formatted/activities/create-formatted-activity-only-required-fields.json",
            "/saved/formatted/activities/get-formatted-activity-only-required-fields.json"
        )
    }
}