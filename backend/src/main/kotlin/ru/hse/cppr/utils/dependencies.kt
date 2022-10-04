package ru.hse.cppr.utils

import org.koin.core.Koin
import org.koin.core.KoinContext
import org.koin.core.parameter.ParameterDefinition
import org.koin.error.ScopeAlreadyExistsException
import org.koin.standalone.KoinComponent
import kotlin.reflect.KClass

inline fun <reified T : Any> KoinComponent.createScope(classOfT: KClass<T>, noinline initParameters: ParameterDefinition) {
    getKoin().createScope(classOfT, initParameters)
}

inline fun <reified T : Any> Koin.createScope(classOfT: KClass<T>, noinline initParameters: ParameterDefinition) {
    koinContext.createScope(classOfT, initParameters)
}

inline fun <reified T : Any> KoinContext.createScope(classOfT: KClass<T>, noinline initParameters: ParameterDefinition) {
    run {
        try {
            get<T>(scope = createScope(classOfT.qualifiedName!!), parameters = initParameters)
        } catch (e: ScopeAlreadyExistsException) {
            get<T>(scope = getScope(classOfT.qualifiedName!!), parameters = initParameters)
        }
    }
}
