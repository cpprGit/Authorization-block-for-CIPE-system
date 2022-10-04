package ru.hse.cppr.representation.enums.fields

enum class AttributeFields(val value: String) {
    ID("id"),
    USAGE("usage"),
    NAME("name"),
    TITLE("title"),
    DESCRIPTION("description"),
    PLACEHOLDER("placeholder"),
    STEP("step"),
    MIN("min"),
    MAX("max"),
    HINT("hint"),
    MANDATORY("mandatory"),
    VALUE_DEFAULT("valueDefault"),
    VARIANTS("variants"),
    VALIDATORS("validators"),
    SEARCH_NAME("searchName"),
    HAS_OTHER_VARIANT("hasOtherVariant")
}