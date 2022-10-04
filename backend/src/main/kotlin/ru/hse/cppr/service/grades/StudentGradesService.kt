package ru.hse.cppr.service.grades

import com.auth0.jwt.interfaces.DecodedJWT
import org.jooq.Configuration
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.service.Service
import java.util.*

interface StudentGradesService: Service {

    fun initGradesProgram(
        studentId: UUID,
        projectId: UUID,
        configuration: Configuration
    )

    fun deleteGradesProgram(
        studentId: UUID,
        projectId: UUID,
        configuration: Configuration
    )

    fun getActivityStudentGradesProgram(
        activityId: UUID,
        studentId: UUID,
        configuration: Configuration
    ): MutableList<Map<String, Any?>>

    fun getActivityGradesFieldsProgram(
        activityId: UUID,
        configuration: Configuration
    ): MutableList<Map<String, Any?>>

    fun getProjectStudentGradesProgram(
        projectId: UUID,
        studentId: UUID,
        configuration: Configuration
    ): MutableList<Map<String, Any?>>

    fun getProjectStudentFilesProgram(
        projectId: UUID,
        studentId: UUID,
        configuration: Configuration
    ): MutableList<Pair<String, Any?>>

    fun getProjectFieldsProgram(
        projectId: UUID,
        configuration: Configuration,
        jwt: DecodedJWT
    ): MutableList<Map<String, Any?>>

    fun updateStudentGrade(
        projectId: UUID,
        studentId: UUID,
        stageId: UUID,
        gradeType: String,
        grade: Int?,
        configuration: Configuration
    )

    fun getStudentStageGrade(
        projectId: UUID,
        studentId: UUID,
        stageId: UUID,
        configuration: Configuration
    ): Map<String, Any>?

}