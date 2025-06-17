package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant

abstract class DeleteItemsAcceptanceContract(
    val doDelete: (App<*>, Set<Item>) -> Unit
)
{
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
    private val fixture = aSampleFixture(lastModified)
    private val app = fixture.createApp(now = sameDayAsLastModified)

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
                sameDayAsLastModified,
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
