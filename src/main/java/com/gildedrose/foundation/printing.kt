@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.gildedrose.foundation

import java.io.PrintStream

inline fun <T> T.printed(to: PrintStream = System.out) =
    also { to.println(this) }

inline fun <T> Iterable<T>.printed(to: PrintStream = System.out) =
    also { to.println(this.joinToString("\n")) }

