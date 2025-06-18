package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant

abstract class DeleteItemsAcceptanceContract(
    val doDelete: (App<*>, Set<Item>) -> Unit
) {
    private val fixture = aSampleFixture(
        stockListLastModified = Instant.parse("2022-02-09T12:00:00Z"),
        now = Instant.parse("2022-02-09T23:59:59Z")
    )
    private val app = fixture.app

    @Test
    fun `delete items`() {
        doDelete(app,
            setOf(
                fixture.originalStockList[0],
                fixture.originalStockList[2],
            )
        )
        fixture.checkStockListIs(
            StockList(
                fixture.now,
                listOf(fixture.originalStockList[1])
            )
        )
    }

    @Test
    fun `delete no items doesnt save stocklist`() {
        doDelete(app,
            emptySet()
        )
        fixture.checkStockListIs(fixture.originalStockList)
    }

    @Test
    open fun `delete non-existent item doesnt save stocklist`() {
        doDelete(app,
            setOf(
                item("no-such", "not in stock", null, 0),
            )
        )
        fixture.checkStockListIs(fixture.originalStockList)
    }
}
