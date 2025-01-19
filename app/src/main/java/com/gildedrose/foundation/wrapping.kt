package com.gildedrose.foundation

typealias Wrapper<R> = (() -> R) -> R

inline fun <R> (() -> R).wrappedWith(
    crossinline wrapper: Wrapper<R>
): () -> R = {
    wrapper { this() }
}

inline fun <T, R> ((T) -> R).wrappedWith(
    crossinline wrapper: Wrapper<R>
): (T) -> R = { it: T ->
    wrapper { this(it) }
}

inline fun <T1, T2, R> ((T1, T2) -> R).wrappedWith(
    crossinline wrapper: Wrapper<R>
): (T1, T2) -> R = { t1: T1, t2: T2 ->
    wrapper { this(t1, t2) }
}

