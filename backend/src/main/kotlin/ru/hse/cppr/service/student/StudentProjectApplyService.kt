package ru.hse.cppr.service.student

import ru.hse.cppr.service.Service

interface StudentProjectApplyService: Service {

    fun applyToProject(body: com.jsoniter.any.Any): Map<String, Any?>

    fun acceptApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?>

    fun declineApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?>

    fun cancelApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?>

    fun cancelAcceptApplyToProject(body: com.jsoniter.any.Any): Map<String, Any?>
}