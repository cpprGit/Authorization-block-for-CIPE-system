package ru.hse.ccpr.application.service.formatted

import io.undertow.util.StatusCodes
import org.junit.jupiter.api.Assertions
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.utils.compareJsonObjects
import ru.hse.ccpr.utils.jsonFromString

class MailGroupsServiceIT: BaseIntegrationTestController() {

    override val path = "/formatted/mail-group/"
    private val listPath = "/formatted/mail-groups/"

    override fun run(): Int {
        return runTests(
            this::testMailGroupIsSavedCorrectly,
            this::testMailGroupList
            )
    }


    private fun testMailGroupIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/mailgroups/create-mail-group.json",
            "/saved/formatted/mailgroups/get-mail-group.json"
        )
    }

    private fun testMailGroupList() {
        val response = getRequest("123e4567-e89b-12d3-a000-000000000000", listPath)
        Assertions.assertEquals(StatusCodes.OK, response.code)

        compareJsonObjects(
            jsonFromString(fromResource("/saved/formatted/mailgroups/get-mail-group-list.json")),
            jsonFromString(response.body?.string().toString())
        )
    }

}