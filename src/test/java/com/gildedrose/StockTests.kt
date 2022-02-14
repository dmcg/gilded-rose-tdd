package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class StockTests {

    private val initialStockList = standardStockList.copy(
        lastModified = Instant.parse("2022-02-09T23:59:59Z")
    )
    private val fixture = Fixture(initialStockList)
    private val stock = Stock(fixture.stockFile, ZoneId.of("Europe/London"))

    @Test fun `loads stock from file`() {
        val now = Instant.parse("2022-02-09T23:59:59Z")
        assertEquals(initialStockList, stock.stockList(now))
    }

    @Test fun `updates stock if last modified yesterday`() {
        val now = Instant.parse("2022-02-10T00:00:01Z")
        val expectedUpdatedResult = initialStockList.copy(lastModified = now)
        assertEquals(expectedUpdatedResult, stock.stockList(now))
        assertEquals(expectedUpdatedResult, fixture.load())
    }
}

class Stock(
    private val stockFile: File,
    private val zoneId: ZoneId
) {
    fun stockList(now: Instant): StockList {
        val loaded = stockFile.loadItems()
        return if (loaded.lastModified.daysTo(now, zoneId) == 0L)
            loaded
        else
            loaded.copy(lastModified = now).also {
                it.saveTo(stockFile)
            }
    }
}

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()
