package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors


class StockTests {

    private val initialStockList = StockList(
        lastModified = Instant.parse("2022-02-09T23:59:59Z"),
        items = listOf(
            Item("banana", oct29.minusDays(1), 42),
            Item("kumquat", oct29.plusDays(1), 101)
        )
    )
    private val fixture = Fixture(initialStockList, now = initialStockList.lastModified)

    private val stock = Stock(
        stockFile = fixture.stockFile,
        zoneId = ZoneId.of("Europe/London"),
        update = ::updateItems
    )

    @Test
    fun `loads stock from file`() {
        val now = Instant.parse("2022-02-09T23:59:59Z")
        assertEquals(initialStockList, stock.stockList(now))
    }

    @Test
    fun `updates stock if last modified yesterday`() {
        val now = Instant.parse("2022-02-10T00:00:01Z")
        val expectedUpdatedResult = StockList(
            lastModified = now,
            items = listOf(
                Item("banana", oct29.minusDays(1), 41),
                Item("kumquat", oct29.plusDays(1), 100)
            )
        )
        assertEquals(expectedUpdatedResult, stock.stockList(now))
        assertEquals(expectedUpdatedResult, fixture.load())
    }

    @Test
    fun `updates stock by two days if last modified the day before yesterday`() {
        val now = Instant.parse("2022-02-11T00:00:01Z")
        val expectedUpdatedResult = StockList(
            lastModified = now,
            items = listOf(
                Item("banana", oct29.minusDays(1), 40),
                Item("kumquat", oct29.plusDays(1), 99)
            )
        )
        assertEquals(expectedUpdatedResult, stock.stockList(now))
        assertEquals(expectedUpdatedResult, fixture.load())
    }

    @Test
    fun `does not update stock if modified tomorrow`() {
        val now = Instant.parse("2022-02-08T00:00:01Z")
        assertEquals(initialStockList, stock.stockList(now))
        assertEquals(initialStockList, fixture.load())
    }

    @Test
    fun `parallel execution`() {
        val count = 8
        val executor = Executors.newFixedThreadPool(count)
        val barrier = CyclicBarrier(count)
        val futures = executor.invokeAll(
            (1..count).map {
                Callable() {
                    barrier.await()
                    `updates stock if last modified yesterday`()
                }
            }
        )
        futures.forEach { it.get() }
    }

    @Test
    fun `sanity check`() {
        for (i in 1 .. 10) {
            `parallel execution`()
        }
    }
}

private fun updateItems(items: List<Item>, days: Int) = items.map { it.copy(quality = it.quality - days) }
