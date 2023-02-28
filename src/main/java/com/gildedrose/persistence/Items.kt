package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

interface Items<TX> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    context(TX) fun load(): Result<StockList, StockListLoadingError>

    fun saveToo(stockList: StockList): RequiresTransaction<TX, StockList, StockListLoadingError.IO> = RequiresTransaction { tx ->
        with(tx) { save(stockList) }
    }

    fun loadToo(): RequiresTransaction<TX, StockList, StockListLoadingError> = RequiresTransaction { tx ->
        with(tx) { load() }
    }
}

class RequiresTransaction<TX, out R, out E>(
    private val f: (TX) -> Result<R, E>
) {
    fun runWith(tx: TX): Result<R, E> {
        return f(tx)
    }
    context(TX) fun run(): Result<R, E> {
        return f(this@TX)
    }
}
