package com.gildedrose.pricing

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import com.gildedrose.item
import com.gildedrose.persistence.NoTX
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.testing.IOResolver
import com.gildedrose.testing.assertFailsWith
import com.gildedrose.testing.assertSucceedsWith
import com.gildedrose.withPriceResult
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localdate

@ExtendWith(IOResolver::class)
class PricedStockListLoaderTests {
    companion object {
        private val lastModified = t("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

        private val loadedStockList = StockList(
            lastModified = lastModified,
            items = listOf(
                item("banana", localdate("2022-02-08"), 42),
                item("kumquat", localdate("2022-02-10"), 101),
                item("undated", null, 50)
            )
        )
        private val expectedPricedStockList = PricedStockList(
            loadedStockList.lastModified,
            items = listOf(
                loadedStockList[0].withPriceResult(Price(666)),
                loadedStockList[1].withPriceResult(null),
                loadedStockList[2].withPriceResult(Price(999))
            )
        )
    }

    private val stockValues = mutableMapOf<Instant, Result<StockList, StockListLoadingError>>(
        sameDayAsLastModified to Success(loadedStockList)
    )
    private val priceList = mutableMapOf<Item, (Item) -> Price?>(
        loadedStockList[0] to { Price(666) },
        loadedStockList[1] to { null },
        loadedStockList[2] to { Price(999) }
    )
    private val analyticsEvents = mutableListOf<AnalyticsEvent>()
    private val loader = PricedStockListLoader(
        { stockValues.getValue(it).bind() },
        pricing = { item -> priceList[item]?.invoke(item) },
        analytics = { event -> analyticsEvents.add(event) }
    )

    context(IO)
    @Test
    fun `loads and prices items`() {
        assertSucceedsWith(expectedPricedStockList) {
            with(NoTX) { loader.load(sameDayAsLastModified) }
        }
        assertTrue(analyticsEvents.isEmpty())
    }

    context(IO)
    @Test
    fun `passes on failures to load stock`() {
        val loadingError: StockListLoadingError = StockListLoadingError.IOError("deliberate")
        stockValues[sameDayAsLastModified] = Failure(loadingError)
        assertFailsWith(loadingError) {
            with(NoTX) { loader.load(sameDayAsLastModified) }
        }
        assertTrue(analyticsEvents.isEmpty())
    }

    context(IO)
    @Test
    fun `item price remembers pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = { throw exception }
        assertSucceedsWith(
            expectedPricedStockList.copy(
                items = expectedPricedStockList.items.toMutableList().apply {
                    set(2, get(2).copy(price = Failure(exception)))
                }
            )) {
            with(NoTX) { loader.load(sameDayAsLastModified) }
        }
        with(analyticsEvents) {
            assertEquals(2, size) // one for the try and the retry
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }

    context(IO)
    @Test
    fun `retries pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = succeedAfter(1, raiseError = { throw exception }) {
            Price(999)
        }
        assertSucceedsWith(expectedPricedStockList) {
            with(NoTX) { loader.load(sameDayAsLastModified) }
        }
        with(analyticsEvents) {
            assertEquals(1, size)
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }
}
