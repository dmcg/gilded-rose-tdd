package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

abstract class AddItemAcceptanceContract(
    private val doAdd: (App<*>, Item) -> Unit
) {
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    protected val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
    protected val fixture = aSampleFixture(lastModified)
    protected val app = fixture.createApp(now = sameDayAsLastModified)

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
