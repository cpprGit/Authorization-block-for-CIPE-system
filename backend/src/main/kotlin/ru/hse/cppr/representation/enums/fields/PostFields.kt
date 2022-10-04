package ru.hse.cppr.representation.enums.fields

enum class PostFields(val value: String) {
    ID("id"),
    CREATED_BY("name"),
    PROFILE("profileId"),
    MESSAGE("message"),
    DATE_CREATED("dateCreated"),
    TEXT("text"),
    FILE("file")
}