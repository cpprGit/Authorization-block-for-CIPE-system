package ru.hse.cppr.service.student

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.StudentProjectApplyFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.grades.StudentGradesService
import java.util.*

class FormattedStudentProjectApplyService(override val serviceName: String) : KoinComponent, StudentProjectApplyService {

    val provider: TxProvider<ForIO>                                                 by inject()
    val log: Log                                                                    by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val studentGradesService: StudentGradesService                          by inject()


    val studentProjectAppliesTable = Tables.STUDENT_PROJECT_REQUEST
    val projectStudents = Tables.PROJECT_STUDENTS
    val projectsTable = Tables.PROJECTS


    override fun applyToProject(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val studentId = UUID.fromString(body[StudentProjectApplyFields.STUDENT_ID.value].toString())
            val projectId = UUID.fromString(body[StudentProjectApplyFields.PROJECT_ID.value].toString())

            val isStudentAlreadyOnThisProject = DSL.using(configuration)
                .selectFrom(projectStudents)
                .where(projectStudents.PROJECT_ID.eq(projectId)
                    .and(projectStudents.STUDENT_ID.eq(studentId)))
                .fetchAny() != null

            if (isStudentAlreadyOnThisProject) {
                throw Exception("User already on project!")
            }

            DSL.using(configuration)
                .insertInto(studentProjectAppliesTable)
                .columns(studentProjectAppliesTable.STUDENT_ID, studentProjectAppliesTable.PROJECT_ID)
                .values(studentId, projectId)
                .returning(studentProjectAppliesTable.STUDENT_ID, studentProjectAppliesTable.PROJECT_ID)
                .fetchOne()
                .let {
                    mapOf(
                        StudentProjectApplyFields.STUDENT_ID.value to it[studentProjectAppliesTable.STUDENT_ID].toString(),
                        StudentProjectApplyFields.PROJECT_ID.value to it[studentProjectAppliesTable.PROJECT_ID].toString()
                    )
                }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun acceptApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val studentUUID = UUID.fromString(body[StudentProjectApplyFields.STUDENT_ID.value].toString())
            val projectUUID = UUID.fromString(body[StudentProjectApplyFields.PROJECT_ID.value].toString())

            DSL.using(configuration)
                .deleteFrom(studentProjectAppliesTable)
                .where(studentProjectAppliesTable.STUDENT_ID.eq(studentUUID)
                    .and(studentProjectAppliesTable.PROJECT_ID.eq(projectUUID)))
                .execute()

            DSL.using(configuration)
                .insertInto(projectStudents)
                .columns(projectStudents.STUDENT_ID, projectStudents.PROJECT_ID)
                .values(studentUUID, projectUUID)
                .execute()

            studentGradesService.initGradesProgram(studentUUID, projectUUID, configuration)
        }

        runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return mapOf(
            "status" to "success"
        )
    }


    override fun declineApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val studentUUID = UUID.fromString(body[StudentProjectApplyFields.STUDENT_ID.value].toString())
            val projectUUID = UUID.fromString(body[StudentProjectApplyFields.PROJECT_ID.value].toString())

            DSL.using(configuration)
                .deleteFrom(studentProjectAppliesTable)
                .where(studentProjectAppliesTable.STUDENT_ID.eq(studentUUID)
                    .and(studentProjectAppliesTable.PROJECT_ID.eq(projectUUID)))
                .execute()
        }

        runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return mapOf(
            "status" to "success"
        )
    }

    override fun cancelApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->
            val studentUUID = UUID.fromString(body[StudentProjectApplyFields.STUDENT_ID.value].toString())
            val projectUUID = UUID.fromString(body[StudentProjectApplyFields.PROJECT_ID.value].toString())

            DSL.using(configuration)
                .deleteFrom(studentProjectAppliesTable)
                .where(
                    studentProjectAppliesTable.STUDENT_ID.eq(studentUUID)
                        .and(studentProjectAppliesTable.PROJECT_ID.eq(projectUUID))
                )
                .execute()

            mapOf("status" to "Success")
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun cancelAcceptApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?> {
        val txResult = provider.tx { configuration ->

            val studentUUID = UUID.fromString(body[StudentProjectApplyFields.STUDENT_ID.value].toString())
            val projectUUID = UUID.fromString(body[StudentProjectApplyFields.PROJECT_ID.value].toString())

            DSL.using(configuration)
                .deleteFrom(projectStudents)
                .where(projectStudents.STUDENT_ID.eq(studentUUID)
                    .and(projectStudents.PROJECT_ID.eq(projectUUID)))
                .execute()

            DSL.using(configuration)
                .insertInto(studentProjectAppliesTable)
                .columns(studentProjectAppliesTable.STUDENT_ID, studentProjectAppliesTable.PROJECT_ID)
                .values(studentUUID, projectUUID)
                .execute()

            studentGradesService.deleteGradesProgram(studentUUID, projectUUID, configuration)

        }

        runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    cb.a.printStackTrace()
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return mapOf("status" to "success")
    }

}