package ru.hse.cppr.representation.enums

enum class UserRoles(val value: String) {
    STUDENT("student"),
    MENTOR("mentor"),
    SUPERVISOR("supervisor"),
    MANAGER("manager"),
    REPRESENTATIVE("representative"),
    ADMIN("admin")
}