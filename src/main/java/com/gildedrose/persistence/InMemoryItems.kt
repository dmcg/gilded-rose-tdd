package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items<Nothing?> {
    private val stockList = AtomicReference(stockList)

    override fun <R> inTransaction(block: context(Nothing?) () -> R) = block(null)

    context(Nothing?) override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError> {
        this@InMemoryItems.stockList.set(stockList)
        return Success(stockList)
    }

    context(Nothing?) override fun load(): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }

}
