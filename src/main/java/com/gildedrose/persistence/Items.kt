package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.flatMap

interface Items<TX> {

    fun <R> inTransaction(block: Reader<TX, R>): R

    context(TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    context(TX) fun load(): Result<StockList, StockListLoadingError>

    fun saveToo(
        stockList: StockList
    ): Reader<TX, Result<StockList, StockListLoadingError.IO>> =
        {
            save(stockList)
        }

    fun loadToo(
    ): Reader<TX, Result<StockList, StockListLoadingError>> =
        {
            load()
        }
}

typealias Reader<C, R> = context(C) () -> R

fun <C, R, R2> (Reader<C, R>).flatMap(
    f: (R) -> Reader<C, R2>
): Reader<C, R2> =
    {
        f(runInContext()).runInContext()
    }

context(C) fun <C, R> Reader<C, R>.runInContext(): R =
    invoke(this@C)

fun <TX, R1, R2, E1, E2 : E1> Reader<TX, Result<R1, E1>>.flatMapResult(
    f: (R1) -> Reader<TX, Result<R2, E2>>
) = flatMap { r1Result ->
    {
        r1Result.flatMap { r1 ->
            f(r1).runInContext()
        }
    }
}

