package ru.hse.cppr.representation.enums.fields

enum class NotificationFields(val value: String) {
    ID("id"),
    FOR_USER("forUser"),
    CREATED_BY("createdBy"),
    ACTION("action"),
    TEXT("text"),
    TARGET("target"),
    TARGET_ID("targetId"),
    TARGET_NAME("targetName"),
    TARGET_TYPE("targeType"),
    TYPE("type"),
    IS_VIEWED("isRead"),
    DATE("date")
}

