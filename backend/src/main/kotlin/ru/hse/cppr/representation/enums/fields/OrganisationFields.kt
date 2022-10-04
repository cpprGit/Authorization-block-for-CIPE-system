package ru.hse.cppr.representation.enums.fields

enum class OrganisationFields(val value: String) {
    ID("id"),
    NAME("orgName"),
    PARENT("parent"),
    IS_HSE_DEPARTMENT("hseDepartment"),
    REPRESENTATIVE_ID("representativeId"),
    LAST_MODIFIED_BY("lastModifiedBy"),
    LAST_MODIFIED_TIME("lastModifiedTime"),
    STATUS("status"),
    REPRESENTATIVES("representatives"),
    MENTORS("mentors")
}