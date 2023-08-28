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
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import java.time.Instant
import kotlin.test.assertEquals

data class Fixture(
    val originalPricedStockList: PricedStockList,
    val items: Items<TXContext> = InMemoryItems()
) {
    val originalStockList = StockList(originalPricedStockList.lastModified,
        items = originalPricedStockList.map { item -> item.withNoPrice() }
    )

    fun init() {
        runIO {
            items.transactionally { save(originalStockList) }
        }
    }
    context(IO)
    fun pricing(item: Item): Price? =
        originalPricedStockList.find { it.withNoPrice() == item }?.price?.valueOrNull()

    context(IO)
    fun checkStockListHas(lastModified: Instant, vararg items: Item) {
        checkStockListIs(StockList(lastModified, items.toList()))
    }

    context(IO)
    fun checkStockListIs(stockList: StockList) {
        assertEquals(
            Success(stockList),
            this.items.transactionally { load() }
        )
    }
}
