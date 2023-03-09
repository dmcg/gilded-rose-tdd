package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.flatMap

interface Items<TX> {

    fun <R> withTransaction(
        block: (tx: TX) -> R
    ): R

    fun save(
        stockList: StockList,
        transaction: TX
    ): Result<StockList, StockListLoadingError.IO>

    fun load(
        transaction: TX
    ): Result<StockList, StockListLoadingError>

    fun save(
        stockList: StockList
    ): Reader<TX, Result<StockList, StockListLoadingError.IO>> =
        Reader { tx ->
            save(stockList, tx)
        }

    fun load(): Reader<TX, Result<StockList, StockListLoadingError>> =
        Reader { tx ->
            load(tx)
        }
}

class Reader<C, R>(
    val f: (C) -> R
) {
    fun runInContext(c: C): R = f(c)

    fun <R2> flatMap(f: (R) -> Reader<C, R2>): Reader<C, R2> =
        Reader { c->
            f(this.f(c)).runInContext(c)
        }
}

fun <C, R1, R2, E1, E2 : E1>
    Reader<C, Result<R1, E1>>.flatMapResult(
    f: (R1) -> Reader<C, Result<R2, E2>>
): Reader<C, Result<R2, E1>> =
    flatMap { r1Result ->
        Reader { c ->
            r1Result.flatMap { r1 ->
                f(r1).runInContext(c)
            }
        }
    }
