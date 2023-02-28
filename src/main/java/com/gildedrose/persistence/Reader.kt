package com.gildedrose.persistence

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class Reader<C, out R, out E>(
    private val  f: context(C) () -> Result<R, E>
) {
    context(C) fun run(): Result<R, E> {
        return f(this@C)
    }
}

fun <C, R1, R2, E> Reader<C, R1, E>.flatMap(f: (R1) -> Reader<C, R2, E>) =
    Reader<C, R2, E> {
        val initialResult = this@flatMap.run()
        when (initialResult) {
            is Failure<E> -> initialResult
            is Success<R1> -> {
                f(initialResult.value).run()
            }
        }
    }
