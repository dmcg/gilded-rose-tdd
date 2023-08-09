@file:Suppress("SUBTYPING_BETWEEN_CONTEXT_RECEIVERS")
package com.gildedrose.foundation

context(C) fun <C> magic() : C = this@C

context(C)
operator fun <C, T, R> (context(C) (T) -> R).invoke(t: T) : R = this(magic<C>(), t)

context(C1, C2, C3)
operator fun <C1, C2, C3, T, R> (context(C1, C2, C3) (T) -> R).invoke(t: T) : R = this(magic<C1>(), magic<C2>(), magic<C3>(), t)
