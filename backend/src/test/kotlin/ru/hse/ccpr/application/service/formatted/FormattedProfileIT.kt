package ru.hse.ccpr.application.service.formatted

import io.undertow.util.StatusCodes
import org.junit.jupiter.api.Assertions.*
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.utils.*

class FormattedProfileIT: BaseIntegrationTestController() {

    override val path = "/formatted/user-profile/"

    override fun run(): Int {
        return runTests(
            this::testGetUserProfileCorrectly)
    }


    private fun testGetUserProfileCorrectly() {
        getProfile(
            "/saved/formatted/profiles/get-student-profile.json"
        )
    }


    private fun getProfile(savedPath: String) {

        val getResponse = getRequest("7f5c1678-61f2-11ea-8cdc-dfb38868a744", path)
        assertEquals(StatusCodes.OK, getResponse.code)

        compareJsonObjects(
            jsonFromString(fromResource(savedPath)),
            jsonFromString(getResponse.body?.string().toString())
        )
    }
}