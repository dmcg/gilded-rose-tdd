package com.gildedrose.testing

import com.gildedrose.domain.Items
import com.gildedrose.domain.NoTX
import com.gildedrose.domain.StockList
import com.gildedrose.domain.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class InMemoryItems(
    stockList: StockList = StockList(Instant.EPOCH, emptyList())
) : Items<NoTX> {
    private val stockList = AtomicReference(stockList)

    override fun <R> inTransaction(block: context(NoTX) () -> R) = block(NoTX)

    context(NoTX) override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError> {
        this@InMemoryItems.stockList.set(stockList)
        return Success(stockList)
    }

    context(NoTX) override fun load(): Result<StockList, StockListLoadingError> {
        return Success(stockList.get())
    }
}
