package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items<NoTX> {
    private val stockList = AtomicReference(stockList)

    override fun <R> inTransaction(block: (NoTX) -> R): R = block(NoTX)

    override fun save(
        stockList: StockList,
        tx: NoTX
    ): Result<StockList, StockListLoadingError.IOError> {
        this@InMemoryItems.stockList.set(stockList)
        return Success(stockList)
    }

    override fun load(tx: NoTX): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }
}
