package ru.hse.cppr.service.studentgroups

import com.jsoniter.any.Any
import org.jooq.Configuration
import ru.hse.cppr.service.Service

interface StudentGroupsService : Service {
    fun updateStudentGroupLists(body: Any)
    fun deleteStudentGroup(groupName: String, configuration: Configuration)
    fun addStudentGroup(groupName: String, configuration: Configuration)
    fun getStudentGroupsProgram(configuration: Configuration): List<String>
    fun getStudentGroups(): List<String>
}