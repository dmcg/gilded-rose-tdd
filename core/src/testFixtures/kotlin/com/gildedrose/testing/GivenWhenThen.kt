@file:Suppress("TestFunctionName")

package com.gildedrose.testing

fun <T> Given(fixture: T): T = fixture

fun <T, R> T.When_(block: T.(T) -> R): R = this.block(this)

fun <T> T.When(block: T.(T) -> Unit): T {
    this.block(this)
    return this
}

fun <T, R> T.Then(block: T.(T) -> R): R = this.block(this)
