package ru.hse.cppr.service.sheets

import com.auth0.jwt.interfaces.DecodedJWT
import ru.hse.cppr.service.Service
import java.util.*

interface SheetService: Service {

    fun getActivitySheet(id: UUID, jwt: DecodedJWT): MutableMap<String, Any>

    fun getProjectSheet(id: UUID, jwt: DecodedJWT): MutableMap<String, Any>

    fun updateStudentGrade(
        projectId: UUID,
        studentId: UUID,
        stageId: UUID,
        gradeType: String,
        grade: Int?
    )
}