package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.theory.Action
import dev.forkhandles.result4k.Result

interface Items<TX> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(TX) @Action
    fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    context(TX) @Action
    fun load(): Result<StockList, StockListLoadingError>
}
