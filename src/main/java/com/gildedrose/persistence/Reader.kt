package com.gildedrose.persistence

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class Reader<TX, out R, out E>(
    private val  f: context(TX) () -> Result<R, E>
) {
    context(TX) fun run(): Result<R, E> {
        return f(this@TX)
    }
}

fun <TX, R1, R2, E> Reader<TX, R1, E>.flatMap(f: (R1) -> Reader<TX, R2, E>) =
    Reader<TX, R2, E> {
        val initialResult = this@flatMap.run()
        when (initialResult) {
            is Failure<E> -> initialResult
            is Success<R1> -> {
                f(initialResult.value).run()
            }
        }
    }
