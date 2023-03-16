package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import dev.forkhandles.result4k.Result

open class TXContext

object NoTX: TXContext()

interface Items<TX: TXContext> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(IO, TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError>

    context(IO, TX) fun load(): Result<StockList, StockListLoadingError>
}
