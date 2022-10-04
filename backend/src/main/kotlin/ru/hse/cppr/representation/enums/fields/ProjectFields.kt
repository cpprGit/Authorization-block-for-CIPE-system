package ru.hse.cppr.representation.enums.fields

enum class ProjectFields(val value: String) {
    ID("id"),
    PROJECT_ID("projectId"),
    CONSULTANT_ID("consultantId"),
    ACTIVITY_ID("activityId"),
    ACTIVITY("activity"),
    PROJECT_NAME_RUS("projectNameRus"),
    PROJECT_NAME_ENG("projectNameEng"),
    PROJECT_TYPE("projectType"),
    PROJECT_INDIVIDUALITY("projectIndividuality"),
    STATUS("status"),
    MENTOR_ID("mentorId"),
    MENTOR("mentor"),
    CONSULTANT("consultant"),
    MAX_STUDENTS("maxStudents"),
    PI_COURSES("piCourses"),
    PMI_COURSES("pmiCourses"),
    PAD_COURSES("padCourses"),
    CREATE_DATE("createDate"),
    CREATED_BY("createdBy")
}