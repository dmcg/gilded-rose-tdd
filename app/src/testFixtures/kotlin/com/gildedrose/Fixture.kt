package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.foundation.magic
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.withNoPrice
import com.gildedrose.testing.withPriceResult
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import java.time.Instant
import java.time.LocalDate.parse
import kotlin.test.assertEquals

data class Fixture(
    val originalPricedStockList: PricedStockList,
    val now: Instant,
    val items: Items<NoTX> = InMemoryItems()
) {
    val originalStockList = StockList(originalPricedStockList.lastModified,
        items = originalPricedStockList.map { item -> item.withNoPrice() }
    )

    val app = App(items, pricing = ::pricing, clock = { now })
    val routes = app.routes

    init {
        items.transactionally { save(originalStockList) }
    }

    private fun pricing(item: Item): Price? =
        originalPricedStockList.find { it.withNoPrice() == item }?.price?.valueOrNull()

    fun checkStockListIs(stockList: StockList) {
        assertEquals(
            Success(stockList),
            this.items.transactionally { load() }
        )
    }
}

fun aSampleFixture(stockListLastModified: Instant, now: Instant) = Fixture(
    PricedStockList(
        stockListLastModified,
        listOf(
            item("banana", parse("2022-02-08"), 42).withPriceResult(Price(666)),
            item("kumquat", parse("2022-02-10"), 101).withPriceResult(null),
            item("undated", null, 50).withPriceResult(Price(999))
        )
    ),
    now
)


private fun <R, TX : TXContext> Items<TX>.transactionally(f: context(TX) Items<TX>.() -> R): R =
    inTransaction {
        f(magic(), this)
    }
