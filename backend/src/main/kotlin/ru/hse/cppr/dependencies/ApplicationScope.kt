package ru.hse.cppr.dependencies

data class ApplicationScope(val propertiesDir: String) {

    companion object {

        @JvmStatic val name: String = ApplicationScope::class.qualifiedName!!
    }
}