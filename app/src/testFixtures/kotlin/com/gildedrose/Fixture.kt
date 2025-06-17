package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.foundation.magic
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.withNoPrice
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import kotlin.test.assertEquals

data class Fixture(
    val originalPricedStockList: PricedStockList,
    val items: Items<NoTX> = InMemoryItems()
) {
    val originalStockList = StockList(originalPricedStockList.lastModified,
        items = originalPricedStockList.map { item -> item.withNoPrice() }
    )

    init {
        items.transactionally { save(originalStockList) }
    }

    fun pricing(item: Item): Price? =
        originalPricedStockList.find { it.withNoPrice() == item }?.price?.valueOrNull()

    fun checkStockListIs(stockList: StockList) {
        assertEquals(
            Success(stockList),
            this.items.transactionally { load() }
        )
    }
}

private fun <R, TX : TXContext> Items<TX>.transactionally(f: context(TX) Items<TX>.() -> R): R =
    inTransaction {
        f(magic(), this)
    }
