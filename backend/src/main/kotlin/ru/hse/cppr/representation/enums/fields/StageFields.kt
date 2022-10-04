package ru.hse.cppr.representation.enums.fields


enum class StageFields(val value: String) {
    ID("id"),
    NAME("name"),
    DESCRIPTION("description"),
    STAGE_NUMBER("stageNumber"),
    GRADE_COEFF("coefficient"),
    MENTOR_GRADE_FINAL("hasForcedGrade"),
    START_DATE("startDate"),
    END_DATE("endDate")
}