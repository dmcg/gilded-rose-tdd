@file:Suppress("unused")

package com.gildedrose.utils

import java.io.PrintStream

inline fun <T> T.printed(to: PrintStream = System.out) = to.println(this)

inline fun <T> Iterable<T>.printed(to: PrintStream = System.out) = to.println(this.joinToString("\n"))
