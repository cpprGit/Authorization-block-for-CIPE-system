package ru.hse.cppr.representation.formats

enum class ProfileTypes(val value: String) {
    ACTIVITY("activity"),
    USER("user"),
    PROJECT("project"),
    ORGANISATION("organisation"),
    PROJECT_REQUEST("project_request"),
    FILE("file")
}