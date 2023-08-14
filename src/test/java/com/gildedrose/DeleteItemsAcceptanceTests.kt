package com.gildedrose

import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.persistence.transactionally
import com.gildedrose.testing.IOResolver
import com.microsoft.playwright.*
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import java.util.regex.Pattern
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

    @Test
    fun main() {
        val port = 8888
        serverFor(port = port, app.routes).start()
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
            )
            val page: Page = browser.newPage()
            page.navigate("http://localhost:$port")

            page.checkBoxFor(pricedStockList[0]).click()
            page.checkBoxFor(pricedStockList[2]).click()

            page.acceptDialog()
            page.deleteButton.click()
            page.waitForResponse(Pattern.compile(".*")) {}

            assertEquals(
                Success(StockList(sameDayAsLastModified, listOf(pricedStockList[1].withNoPrice()))),
                fixture.unpricedItems.transactionally { load() }
            )

            Thread.sleep(100)
            val renderedPage = page.content()
            page.reload()
            assertEquals(
                page.content().withNoEmptyLines(),
                renderedPage.withNoEmptyClassAttributes().withNoEmptyLines()
            )
        }
    }

}

private fun Page.checkBoxFor(
    pricedItem: PricedItem
): Locator = locator("""input[name="${pricedItem.id}"][type="checkbox"]""")

private fun Page.acceptDialog() {
    onDialog { dialog: Dialog ->
        dialog.accept()
    }
}

private val Page.deleteButton: Locator
    get() = locator("""input[value="Delete"][type="submit"]""")

private fun String.withNoEmptyClassAttributes() = replace(""" class=""""", "")

private fun String.withNoEmptyLines(): String {
    return this.lines().filter { it.isNotBlank() }.joinToString("\n")
}
