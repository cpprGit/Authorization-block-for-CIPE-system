package ru.hse.cppr.service.profile

import ru.hse.cppr.service.Service
import java.util.*

interface ProjectProfileForStudentService: Service {

    fun getProfile(projectId: UUID, studentId: UUID): Map<String, Any?>
}