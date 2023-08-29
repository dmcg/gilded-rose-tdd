package com.gildedrose

import com.gildedrose.config.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.testing.IOResolver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

context(IO)
@ExtendWith(IOResolver::class)
abstract class AddItemAcceptanceContract(
    private val doAdd: (App, Item) -> Unit
) {
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    protected val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
    private val pricedStockList = PricedStockList(
        lastModified,
        listOf(
            item("banana", LocalDate.parse("2022-02-08"), 42).withPriceResult(Price(666)),
        )
    )
    protected val fixture = Fixture(pricedStockList, InMemoryItems()).apply { init() }
    protected val app = App(
        items = fixture.items,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified },
        features = Features(newItemEnabled = true)
    )

    @Test
    fun `add item`() {
        val newItem = item("new-id", "new name", LocalDate.parse("2023-07-23"), 99)
        doAdd(app, newItem)
        fixture.checkStockListIs(
            StockList(
                sameDayAsLastModified,
                fixture.originalStockList + newItem
            )
        )
    }

    @Test
    fun `add item with no date`() {
        val newItem = item("new-id", "new name", null, 99)
        doAdd(app, newItem)
        fixture.checkStockListIs(
            StockList(
                sameDayAsLastModified,
                fixture.originalStockList + newItem
            )
        )
    }
}
