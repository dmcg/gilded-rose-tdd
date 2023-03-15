package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.theory.Action
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items<Nothing?> {
    private val stockList = AtomicReference(stockList)

    override fun <R> inTransaction(block: context(Nothing?) () -> R) = block(null)

    context(Nothing?) @Action
    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO> {
        this@InMemoryItems.stockList.set(stockList)
        return Success(stockList)
    }

    context(Nothing?) @Action
    override fun load(): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }

}
