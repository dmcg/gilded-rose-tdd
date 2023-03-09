package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

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
}
