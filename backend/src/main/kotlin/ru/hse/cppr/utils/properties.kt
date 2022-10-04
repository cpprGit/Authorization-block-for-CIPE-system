package ru.hse.cppr.utils

import arrow.core.Either
import arrow.core.Tuple2
import arrow.fx.typeclasses.Concurrent
import arrow.mtl.ReaderT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

typealias PropertyKey   = Tuple2<String, String>
typealias PropertyValue = String

fun <F> getProperty(MA: Concurrent<F>, props: String, key: PropertyKey, ctx: CoroutineContext = Dispatchers.IO): ReaderT<F, (String, PropertyKey) -> PropertyValue, PropertyValue> = ReaderT { readProperty ->

    return@ReaderT MA.cancelable<String> { proc ->
        val deferred = GlobalScope.async(ctx) {
            Either.catch { readProperty(props, key) }
                .let(proc)
        }

        MA.later { deferred.cancel() }
    }
}

fun getPropertySync(props: String, key: PropertyKey): PropertyValue {
    val file = File(props)
    val properties = Properties()
    properties.load(file.inputStream())
    return properties.getProperty(key.b)
}