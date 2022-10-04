package ru.hse.ccpr.application.service.formatted

import io.undertow.util.StatusCodes
import org.junit.jupiter.api.Assertions.*
import ru.hse.ccpr.application.BaseIntegrationTestController
import ru.hse.ccpr.utils.*

class FormattedSchemaServiceIT: BaseIntegrationTestController() {

    override val path = "/formatted/schema/"

    override fun run(): Int {
        return runTests(
            this::testSchemaIsSavedCorrectly,
            this::testSchemaIsSavedCorrectlyRequiredFieldsOnly,
            this::testActivitySchemaIsSavedCorrectly,
            this::testProjectRequestSchemaIsSavedCorrectly,
            this::testStudentRegistrationSchemaIsSavedCorrectly,
            this::testWorkerRegistrationSchemaIsSavedCorrectly)
    }


    private fun testSchemaIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-schema.json",
            "/saved/formatted/schemas/get-formatted-schema.json"
        )
    }

    private fun testSchemaIsSavedCorrectlyRequiredFieldsOnly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-schema-only-required-fields.json",
            "/saved/formatted/schemas/get-formatted-schema-only-required-fields.json"
        )
    }

    private fun testActivitySchemaIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-activity-schema.json",
            "/saved/formatted/schemas/get-formatted-activity-schema.json"
        )
    }

    private fun testProjectRequestSchemaIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-project-request-schema.json",
            "/saved/formatted/schemas/get-formatted-project-request-schema.json"
        )
    }

    private fun testStudentRegistrationSchemaIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-student-registration-schema.json",
            "/saved/formatted/schemas/get-formatted-student-registration-schema.json"
        )
    }

    private fun testWorkerRegistrationSchemaIsSavedCorrectly() {
        createAndGetResource(
            "/requests/formatted/schemas/create-formatted-worker-registration-schema.json",
            "/saved/formatted/schemas/get-formatted-worker-registration-schema.json"
        )
    }
}