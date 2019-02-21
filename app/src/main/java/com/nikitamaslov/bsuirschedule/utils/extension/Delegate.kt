@file: [JvmName("Extensions") JvmMultifileClass]

package com.nikitamaslov.bsuirschedule.utils.extension

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun <T> closeable(closed: KProperty0<Boolean>, init: () -> T): Closeable<T> = Closeable(closed, init)

class Closeable <T> (
    private val closed: KProperty0<Boolean>,
    private val provider: () -> T
) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        provider().takeIf { !closed.get() }
            ?: throw AssertionError("illegal access to closed property '$property'")

}