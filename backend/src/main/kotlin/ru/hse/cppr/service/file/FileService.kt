package ru.hse.cppr.service.file

import org.jooq.Configuration
import ru.hse.cppr.service.Service
import java.util.*

interface FileService : Service {

    fun saveFile(requestBody: ByteArray, taskId: UUID?, uploaderId: UUID?): String

    fun downloadFile(fileId: UUID): Map<String, String>?

    fun getFilesForProfile(id: UUID): MutableList<Map<String, kotlin.Any>>

    fun getFileForTask(id: UUID): Map<String, Any>?

    fun getFileForTaskAndUser(taskId: UUID, userId: UUID): Map<String, Any>?

    fun deleteFile(id: UUID): Map<String, Any>

    fun deleteFile(id: UUID, configuration: Configuration): Map<String, Any>
}