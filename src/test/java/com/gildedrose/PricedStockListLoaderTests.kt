package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.UncaughtExceptionEvent
import com.gildedrose.foundation.succeedAfter
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localdate

class PricedStockListLoaderTests {
    companion object {
        private val lastModified = t("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

        private val loadedStockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", localdate("2022-02-08"), 42),
                testItem("kumquat", localdate("2022-02-10"), 101),
                testItem("undated", null, 50)
            )
        )
        private val expectedPricedStockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", localdate("2022-02-08"), 42).copy(price = Success(Price(666))),
                testItem("kumquat", localdate("2022-02-10"), 101).copy(price = Success(null)),
                testItem("undated", null, 50).copy(price = Success(Price(999)))
            )
        )
    }

    private val stockValues = mutableMapOf<Instant, StockLoadingResult>(
        sameDayAsLastModified to Success(loadedStockList)
    )
    private val priceList = mutableMapOf<Item, (Item) ->Price?>(
        loadedStockList[0] to { Price(666) },
        loadedStockList[2] to { Price(999) }
    )
    private val analyticsEvents = mutableListOf<AnalyticsEvent>()
    private val loader = PricedStockListLoader(
        stockValues::getValue,
        pricing = { item -> priceList[item]?.invoke(item) },
        analytics = { event -> analyticsEvents.add(event) }
    )

    @Test
    fun `loads and prices items`() {
        assertEquals(
            Success(expectedPricedStockList),
            loader.load(sameDayAsLastModified)
        )
        assertTrue(analyticsEvents.isEmpty())
    }

    @Test
    fun `passes on failures to load stock`() {
        val loadingError = StockListLoadingError.IO("deliberate")
        stockValues[sameDayAsLastModified] = Failure(loadingError)
        assertEquals(
            Failure(loadingError),
            loader.load(sameDayAsLastModified)
        )
        assertTrue(analyticsEvents.isEmpty())
    }

    @Test
    fun `item price remembers pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = { throw exception }
        assertEquals(
            Success(
                expectedPricedStockList.copy(
                    items = expectedPricedStockList.items.toMutableList().apply {
                        set(2, get(2).copy(price = Failure(exception)))
                    }
                )
            ),
            loader.load(sameDayAsLastModified)
        )
        with(analyticsEvents) {
            assertEquals(2, size) // one for the try and the retry
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }

    @Test
    fun `retries pricing failures`() {
        val exception = Exception("deliberate")
        priceList[loadedStockList[2]] = succeedAfter(1, raiseError = { throw exception }) {
            Price(999)
        }
        assertEquals(
            Success(expectedPricedStockList),
            loader.load(sameDayAsLastModified)
        )
        with(analyticsEvents) {
            assertEquals(1, size)
            assertTrue(all { it is UncaughtExceptionEvent })
        }
    }
}
