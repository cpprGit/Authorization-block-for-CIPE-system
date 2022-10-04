package ru.hse.cppr.service.users

import com.jsoniter.any.Any
import ru.hse.cppr.service.Service
import java.util.*

interface UsersService: Service {

    fun persistStudent(bodyJson: Any): Map<String, String?>

    fun persistUser(bodyJson: Any): Any
    fun getUser(id: UUID): Map<String, kotlin.Any?>
    fun deleteUser(id: UUID)
    fun deleteStudent(id: UUID)
}