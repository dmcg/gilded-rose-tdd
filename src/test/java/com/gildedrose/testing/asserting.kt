package com.gildedrose.testing

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
