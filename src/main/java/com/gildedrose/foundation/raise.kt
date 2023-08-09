package com.gildedrose.foundation

import arrow.core.nonFatalOrThrow
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.fold
import arrow.core.raise.merge
import dev.forkhandles.result4k.*

inline fun <R, E> result4k(block: Raise<E>.() -> R): Result4k<R, E> =
    fold(block, ::Failure, ::Success)

// This doesn't catch fatal exceptions, like coroutine cancellation or raised errors, which is good.
inline fun <R> resultCatch(block: () -> R): Result4k<R, Exception> =
    resultFrom(block).peekFailure { it.nonFatalOrThrow() }

context(Raise<E>)
fun <R, E> Result4k<R, E>.bind(): R = recover(::raise)

context(Raise<Error>)
inline fun <reified Exception : Throwable, Error, Result> withException(
    transform: (Exception) -> Error,
    block: () -> Result
): Result = catch<Exception, Result>(block) { raise(transform(it)) }

inline fun ignoreErrors(block: Raise<Any?>.() -> Unit) {
  merge(block)
}
