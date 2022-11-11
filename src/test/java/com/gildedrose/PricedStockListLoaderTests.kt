package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localdate

class PricedStockListLoaderTests {
    companion object {
        private val lastModified = t("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", localdate("2022-02-08"), 42),
                testItem("kumquat", localdate("2022-02-10"), 101),
                testItem("undated", null, 50)
            )
        )
    }

    private val stockValues = mutableMapOf<Instant, Result<StockList, StockListLoadingError>>()
    private val priceList = mutableMapOf<Item, Price?>()
    private val loader = PricedStockListLoader(stockValues::getValue, priceList::get)

    @Test
    fun `loads and prices items`() {
        stockValues[sameDayAsLastModified] = Success(stockList)
        priceList.putAll(
            mapOf(
                stockList[0] to Price(666),
                stockList[2] to Price(999)
            )
        )
        assertEquals(
            Success(
                StockList(
                    lastModified = lastModified,
                    items = listOf(
                        testItem("banana", localdate("2022-02-08"), 42).copy(price = Success(Price(666))),
                        testItem("kumquat", localdate("2022-02-10"), 101).copy(price = Success(null)),
                        testItem("undated", null, 50).copy(price = Success(Price(999)))
                    )
                )
            ),
            loader.load(sameDayAsLastModified)
        )
    }
}
