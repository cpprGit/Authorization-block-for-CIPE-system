package ru.hse.ccpr.application.service.formatted

import io.undertow.util.StatusCodes
import org.junit.jupiter.api.Assertions.*
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.utils.*

class AttributesServiceIT: BaseIntegrationTestController() {

    override val path = "/attribute/"

    override fun run(): Int {
        return runTests(
            this::testAttributeIsSavedCorrectly,
            this::testAttributeOnlyRequiredFieldsIsSavedCorrectly
            )
    }


    private fun testAttributeIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/attributes/create-attribute.json",
            "/saved/formatted/attributes/get-attribute.json"
        )
    }



    private fun testAttributeOnlyRequiredFieldsIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/attributes/create-attribute-required-fields-only.json",
            "/saved/formatted/attributes/get-attribute-required-fields-only.json"
        )
    }

}