package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items {
    private val stockList = AtomicReference(stockList)

    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO> {
        this.stockList.set(stockList)
        return Success(stockList)
    }

    override fun load()
    : Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }

}
