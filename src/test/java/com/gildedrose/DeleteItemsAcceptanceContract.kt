package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.testing.IOResolver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

context(com.gildedrose.foundation.IO)
@ExtendWith(IOResolver::class)
abstract class DeleteItemsAcceptanceContract(
    val doDelete: (App, Set<Item>) -> Unit
)
{
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    protected val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
    val pricedStockList = PricedStockList(
        lastModified,
        listOf(
            item("banana", LocalDate.parse("2022-02-08"), 42).withPriceResult(Price(666)),
            item("kumquat", LocalDate.parse("2022-02-10"), 101).withPriceResult(null),
            item("undated", null, 50).withPriceResult(Price(999))
        )
    )
    val fixture = Fixture(pricedStockList, InMemoryItems()).apply { init() }
    val app = App(
        items = fixture.items,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified }
    )


    @Test
    fun `delete items`() {
        val toDelete = setOf(
            fixture.originalStockList[0],
            fixture.originalStockList[2],
        )

        doDelete(app, toDelete)

        fixture.checkStockListIs(
            StockList(
                sameDayAsLastModified,
                listOf(fixture.originalStockList[1])
            )
        )
    }

    @Test
    fun `delete no items doesnt save stocklist`() {
        val toDelete = emptySet<Item>()

        doDelete(app, toDelete)

        fixture.checkStockListIs(fixture.originalStockList)
    }

    @Test
    open fun `delete non-existent item doesnt save stocklist`() {
        val toDelete = setOf(
            item("no-such", "not in stock", null, 0),
        )

        doDelete(app, toDelete)

        fixture.checkStockListIs(fixture.originalStockList)
    }
}
