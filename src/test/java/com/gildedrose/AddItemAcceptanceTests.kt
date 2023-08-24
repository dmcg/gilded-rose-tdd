package com.gildedrose

import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.testing.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate

context(com.gildedrose.foundation.IO)
@ExtendWith(IOResolver::class)
class AddItemAcceptanceTests {

    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")

    private val pricedStockList = PricedStockList(
        lastModified,
        listOf(
            item("banana", LocalDate.parse("2022-02-08"), 42).withPriceResult(Price(666)),
        )
    )
    private val fixture = Fixture(pricedStockList, InMemoryItems()).apply { init() }
    private val app = App(
        items = fixture.unpricedItems,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified }
    )

    private val showRunning = false

    @Test
    fun main() {
        runWithPlaywright(
            app.routes,
            launchOptions = launchOptions(showRunning)
        ) {
            inputNamed("new-itemId").type("new-id")
            inputNamed("new-itemName").type("new name")
            inputNamed("new-itemSellBy").type("24-08-2023")
            inputNamed("new-itemQuality").type("99")

            waitingForHtmx {
                submitButtonNamed("Add").click()
            }

            checkReloadsTheSame()
            fixture.checkStocklistHas(
                sameDayAsLastModified,
                fixture.stockList[0],
                item("new-id", "new name", LocalDate.parse("2023-08-24"), 99)
            )
        }
    }
}
