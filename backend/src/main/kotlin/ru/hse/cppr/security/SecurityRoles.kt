package ru.hse.cppr.security

import ru.hse.cppr.representation.enums.UserRoles

object SecurityRoles {

    val ALL_USERS = arrayOf(UserRoles.SUPERVISOR, UserRoles.MANAGER, UserRoles.STUDENT, UserRoles.MENTOR, UserRoles.REPRESENTATIVE)
    val CPPW = arrayOf(UserRoles.SUPERVISOR, UserRoles.MANAGER)
    val CPPW_MENTORS = arrayOf(UserRoles.SUPERVISOR, UserRoles.MANAGER, UserRoles.MENTOR)
    val CPPW_M_R = arrayOf(UserRoles.SUPERVISOR, UserRoles.MANAGER, UserRoles.REPRESENTATIVE)
    val STUDENTS = arrayOf(UserRoles.STUDENT)
    val SUPERVISOR = arrayOf(UserRoles.SUPERVISOR)

}