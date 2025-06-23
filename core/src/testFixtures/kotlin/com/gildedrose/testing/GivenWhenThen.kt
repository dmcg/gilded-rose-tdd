package com.gildedrose.testing

@JvmInline
value class Given<F>(val fixture: F) {
    fun <R> When(block: (F).(F) -> R): WhenThenState<F, R> = WhenThenState(fixture, fixture.block(fixture))
}

data class WhenThenState<F, R>(
    val fixture: F,
    val result: R
) {
    fun <R2> Then(block: (F).(R) -> R2): WhenThenState<F, R2> =
        WhenThenState(this.fixture, this.fixture.block(this.result))
    fun <R2> When(function: (F).(F) -> R2): WhenThenState<F, R2> =
        WhenThenState(this.fixture, this.fixture.function(this.fixture))
}
