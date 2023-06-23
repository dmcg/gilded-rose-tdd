package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.runIO
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.TXContext
import com.gildedrose.persistence.transactionally
import dev.forkhandles.result4k.valueOrNull

data class Fixture(
    val pricedStockList: PricedStockList,
    val unpricedItems: Items<TXContext> = InMemoryItems()
) {
    val stockList = StockList(pricedStockList.lastModified,
        items = pricedStockList.map { item -> item.withNoPrice() }
    )

    fun init() {
        runIO {
            unpricedItems.transactionally { save(stockList) }
        }
    }
    context(IO)
    fun pricing(item: Item): Price? =
        pricedStockList.find { it.withNoPrice() == item }?.price?.valueOrNull()

}
