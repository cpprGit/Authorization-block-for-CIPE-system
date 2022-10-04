package ru.hse.cppr.service.sheets

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.StageFields
import ru.hse.cppr.representation.enums.fields.StudentGradeFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.grades.StudentGradesService
import java.util.*
import kotlin.math.roundToInt

class SheetServiceImpl(override val serviceName: String) : KoinComponent, SheetService {

    private val provider: TxProvider<ForIO>                               by inject()
    private val log: Log                                                  by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val studentGradesService: StudentGradesService                by inject()

    private val usersTable = Tables.USERS
    private val projectsTable = Tables.PROJECTS
    private val activityStudentsTable = Tables.ACTIVITY_STUDENT
    private val projectStudentTable = Tables.PROJECT_STUDENTS


    override fun getActivitySheet(id: UUID, jwt: DecodedJWT): MutableMap<String, Any> {
        val sheetM = provider.tx { configuration ->
            val fields = studentGradesService.getActivityGradesFieldsProgram(id, configuration)
            val records = mutableListOf<Map<String, Any?>>()

            for (studentId in getActivityStudents(id, configuration)) {
                val studentRecord = mutableMapOf<String, Any?>()
                val studentGrades = studentGradesService.getActivityStudentGradesProgram(id, studentId, configuration)

                var finalGrade = 0.0
                for (grade in studentGrades) {
                    studentRecord[grade[StageFields.NAME.value].toString()] = grade[StudentGradeFields.GRADE.value]

                    val gradeInt =
                        if (grade[StudentGradeFields.GRADE.value].toString().isEmpty()) 0 else grade[StudentGradeFields.GRADE.value].toString().toInt()

                    finalGrade += gradeInt * grade[StageFields.GRADE_COEFF.value].toString().toDouble()
                }

                studentRecord["finalGrade"] = finalGrade.roundToInt().toString()

                studentRecord["project"] = getStudentProject(id, studentId, configuration)
                studentRecord["mentor"] = getStudentMentorByActivity(id, studentId, configuration)
                studentRecord["student"] = getStudent(studentId, configuration)

                records.add(studentRecord)
            }

            val result = mutableMapOf<String, Any>()

            result["fields"] = fields
            result["records"] = records

            result

        }

        return runBlocking {
            when (val cb = Either.catch { sheetM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    override fun getProjectSheet(id: UUID, jwt: DecodedJWT): MutableMap<String, Any> {
        val sheetM = provider.tx { configuration ->
            val fields = studentGradesService.getProjectFieldsProgram(id, configuration, jwt)
            val records = mutableListOf<Map<String, Any?>>()

            for (studentId in getProjectStudents(id, configuration)) {
                val studentRecord = mutableMapOf<String, Any?>()
                val studentGrades = studentGradesService.getProjectStudentGradesProgram(id, studentId, configuration)

                for (grade in studentGrades) {
                    studentRecord[grade[StageFields.NAME.value].toString()] = grade[StudentGradeFields.GRADE.value]
                }

                for (file in studentGradesService.getProjectStudentFilesProgram(id, studentId, configuration)) {
                    studentRecord[file.first] = file.second
                }


                studentRecord["mentor"] = getProjectMentor(id,configuration)
                studentRecord["student"] = getStudent(studentId, configuration)

                records.add(studentRecord)
            }

            val result = mutableMapOf<String, Any>()

            result["fields"] = fields
            result["records"] = records

            result

        }

        return runBlocking {
            when (val cb = Either.catch { sheetM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun updateStudentGrade(projectId: UUID, studentId: UUID, stageId: UUID, gradeType: String, grade: Int?) {
        val sheetM = provider.tx { configuration ->
            studentGradesService.updateStudentGrade(projectId, studentId, stageId, gradeType, grade, configuration)
        }

        runBlocking {
            when (val cb = Either.catch { sheetM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    private fun getStudentProject(activityId: UUID, studentId: UUID, configuration: Configuration): Map<String, Any>? {
        val result =  DSL.using(configuration)
            .select()
            .from(projectStudentTable)
            .innerJoin(projectsTable)
            .on(projectStudentTable.PROJECT_ID.eq(projectsTable.ID))
            .where(projectsTable.ACTIVITY_ID.eq(activityId)
                .and(projectStudentTable.STUDENT_ID.eq(studentId)))
            .fetchOne()


        return when(result) {
            null -> null
            else -> result.map {
                mapOf(
                    CommonFields.ID.value to it[projectsTable.ID].toString(),
                    CommonFields.NAME.value to it[projectsTable.NAME_RUS].toString(),
                    CommonFields.TYPE.value to ProfileTypes.PROJECT.value
                )
            }
        }
    }

    private fun getStudent(id: UUID, configuration: Configuration): Map<String, Any> {
        return DSL.using(configuration)
            .select()
            .from(usersTable)
            .where(usersTable.ID.eq(id))
            .fetchOne()
            .map {
                mapOf(
                    CommonFields.ID.value to it[usersTable.ID].toString(),
                    CommonFields.NAME.value to it[usersTable.NAME].toString(),
                    CommonFields.TYPE.value to ProfileTypes.USER.value
                )
            }
    }

    private fun getStudentMentorByActivity(activityId: UUID, studentId: UUID, configuration: Configuration): Map<String, Any>? {
        val result = DSL.using(configuration)
            .select()
            .from(projectStudentTable)
            .innerJoin(projectsTable)
            .on(projectStudentTable.PROJECT_ID.eq(projectsTable.ID))
            .innerJoin(usersTable)
            .on(usersTable.ID.eq(projectsTable.LEADER_ID))
            .where(projectsTable.ACTIVITY_ID.eq(activityId)
                .and(projectStudentTable.STUDENT_ID.eq(studentId)))
            .fetchOne()



        return when(result) {
            null -> null
            else -> result.map {
                mapOf(
                    CommonFields.ID.value to it[usersTable.ID].toString(),
                    CommonFields.NAME.value to it[usersTable.NAME].toString(),
                    CommonFields.TYPE.value to ProfileTypes.USER.value
                )
            }
        }
    }

    private fun getProjectMentor(id: UUID, configuration: Configuration): Map<String, Any> {
        return DSL.using(configuration)
            .select()
            .from(projectsTable)
            .innerJoin(usersTable)
            .on(projectsTable.LEADER_ID.eq(usersTable.ID))
            .where(projectsTable.ID.eq(id))
            .fetchOne()
            .map {
                mapOf(
                    CommonFields.ID.value to it[usersTable.ID].toString(),
                    CommonFields.NAME.value to it[usersTable.NAME].toString(),
                    CommonFields.TYPE.value to ProfileTypes.USER.value
                )
            }

    }


    private fun getActivityStudents(id: UUID, configuration: Configuration): List<UUID> {
        val students =
            DSL.using(configuration)
                .select()
                .from(projectStudentTable)
                .innerJoin(projectsTable)
                .on(projectsTable.ID.eq(projectStudentTable.PROJECT_ID))
                .where(projectsTable.ACTIVITY_ID.eq(id))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[projectStudentTable.STUDENT_ID])

                    list
                }

        val extraStudents =
            DSL.using(configuration)
                .selectFrom(activityStudentsTable)
                .where(activityStudentsTable.ACTIVITY_ID.eq(id))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[activityStudentsTable.STUDENT_ID])

                    list
                }

        val result = (students + extraStudents)
        result.distinct()

        return  result.distinct()
    }

    private fun getProjectStudents(id: UUID, configuration: Configuration): MutableList<UUID> {
        return DSL.using(configuration)
            .selectFrom(projectStudentTable)
            .where(projectStudentTable.PROJECT_ID.eq(id))
            .fetch()
            .fold(mutableListOf<UUID>()) { list, record ->
                list.add(record[projectStudentTable.STUDENT_ID])

                list
            }

    }

}