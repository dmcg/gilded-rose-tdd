package com.gildedrose

import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.persistence.transactionally
import com.gildedrose.testing.*
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

context(com.gildedrose.foundation.IO)
@ExtendWith(IOResolver::class)
class DeleteItemsAcceptanceTests {

    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")

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
        items = fixture.unpricedItems,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified }
    )

    val showRunning = false
    private val launchOptions =
        if (showRunning)
            LaunchOptions().setHeadless(false).setSlowMo(250.0)
        else null

    @Test
    fun main() {
        runWithPlaywright(
            app.routes,
            launchOptions = launchOptions
        ) {
            checkBoxFor(pricedStockList[0]).click()
            checkBoxFor(pricedStockList[2]).click()

            waitingForHtmx {
                acceptNextDialog()
                deleteButton.click()
            }

            checkReloadsTheSame()
            assertEquals(
                Success(StockList(sameDayAsLastModified, listOf(pricedStockList[1].withNoPrice()))),
                fixture.unpricedItems.transactionally { load() }
            )
        }
    }
}

private fun Page.checkBoxFor(
    pricedItem: PricedItem
): Locator = locator("""input[name="${pricedItem.id}"][type="checkbox"]""")

private val Page.deleteButton: Locator
    get() = locator("""input[value="Delete"][type="submit"]""")

