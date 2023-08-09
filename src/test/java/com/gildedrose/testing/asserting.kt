@file:OptIn(ExperimentalTypeInference::class)
package com.gildedrose.testing

import arrow.core.raise.Raise
import arrow.core.raise.merge
import arrow.core.raise.recover
import com.gildedrose.foundation.magic
import kotlin.experimental.ExperimentalTypeInference
import kotlin.test.assertEquals

fun <T> assertAll(subject: T, vararg assertions: (T).() -> Unit) {
    assertAll(subject, assertions.toList())
}

fun <T> assertAll(subject: T, assertions: Iterable<(T).() -> Unit>) {
    with(subject) {
        val convertedAssertions: List<() -> Unit> = assertions.map { assertion ->
            { assertion() }
        }
        org.junit.jupiter.api.assertAll(this.toString(), convertedAssertions)
    }
}

inline fun <E, R> assertSucceeds(@BuilderInference block: Raise<E>.() -> R): R =
    recover(block) { error("Unexpected raised error: $it") }

inline fun <E, R> assertSucceedsWith(result: R, @BuilderInference block: Raise<E>.() -> R) {
  assertEquals(result, assertSucceeds(block))
}

inline fun <E, R> assertFails(@BuilderInference block: Raise<E>.() -> R): E =
    merge {
        block(magic())
        error("Expected raised error")
    }

inline fun <E, R> assertFailsWith(error: E, @BuilderInference block: Raise<E>.() -> R) {
    assertEquals(error, assertFails(block))
}
