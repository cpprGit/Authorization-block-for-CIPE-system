package ru.hse.cppr.utils

import org.jooq.*

fun <A: Record, T> UpdateSetMoreStep<A>.setOpt(field: Field<T>, value: T?): UpdateSetMoreStep<A> = when (value) {
    null -> setNull(field)
    else -> set(field, value)
}

fun <A: Record, T> UpdateSetStep<A>.setOpt(field: Field<T>, value: T?): UpdateSetStep<A> = when (value) {
    null -> setNull(field)
    else -> set(field, value)
}

fun <A: Record, T> UpdateSetFirstStep<A>.setOpt(field: Field<T>, value: T?): UpdateSetStep<A> = when (value) {
    null -> setNull(field)
    else -> set(field, value)
}