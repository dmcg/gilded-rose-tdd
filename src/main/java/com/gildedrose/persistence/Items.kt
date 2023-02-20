package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

interface Items {
    fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    fun load(): Result<StockList, StockListLoadingError>
}
