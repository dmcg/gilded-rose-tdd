package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

interface Items<TX> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    context(TX) fun load(): Result<StockList, StockListLoadingError>

    fun saveToo(stockList: StockList): Reader<TX, StockList, StockListLoadingError.IO> = Reader {
        save(stockList)
    }

    fun loadToo(): Reader<TX, StockList, StockListLoadingError> = Reader {
        load()
    }
}

