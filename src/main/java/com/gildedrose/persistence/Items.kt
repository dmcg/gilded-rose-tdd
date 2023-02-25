package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

interface Items<TX> {
    context(TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO>

    context(TX) fun load(): Result<StockList, StockListLoadingError>
}
