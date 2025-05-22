@file:Suppress("CONTEXT_RECEIVERS_DEPRECATED")

package com.gildedrose.foundation

context(C) fun <C> magic() : C = this@C

// replaced with wrappedWith
fun <C, T, R> (context(C) (T) -> R).transformedBy(
    transform: ((T) -> R) -> (T) -> R
): context(C) (T) -> R = { outerIt ->
    transform { innerIt: T ->
        this(magic(), innerIt)
    }(outerIt)
}
