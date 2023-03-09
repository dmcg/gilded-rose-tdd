package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items<Unit> {
    private val stockList = AtomicReference(stockList)

    override fun <R> withTransaction(
        block: (tx: Unit) -> R
    ): R = block(Unit)

    override fun save(
        stockList: StockList,
        transaction: Unit
    ): Result<StockList, StockListLoadingError.IO> {
        this.stockList.set(stockList)
        return Success(stockList)
    }

    override fun load(
        transaction: Unit
    )
    : Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }
}
