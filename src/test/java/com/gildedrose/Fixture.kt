package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.runIO
import com.gildedrose.persistence.InMemoryItems
import dev.forkhandles.result4k.valueOrNull

data class Fixture(
    val pricedStockList: StockList
) {
    context(IO)
    fun pricing(item: Item): Price? =
        pricedStockList.find { it.withPrice(null) == item }?.price?.valueOrNull()

    val stockList = pricedStockList.copy(
        items = pricedStockList.map { item -> item.withPrice(null) }
    )

    val unpricedItems: InMemoryItems = InMemoryItems().apply {
        runIO {
            inTransaction { save(stockList) }
        }
    }
}
