package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.item
import com.gildedrose.oct29
import dev.forkhandles.result4k.valueOrNull
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
            item("banana", oct29.minusDays(1), 42),
            item("kumquat", oct29.plusDays(1), 101)
        )
    )
    private val items = InMemoryItems(initialStockList)

    private val stock = Stock(
        items,
        zoneId = ZoneId.of("Europe/London"),
        itemUpdate = { days, _ -> this.copy(quality = this.quality - days) }
    )

    @Test
    fun `loads stock from file`() {
        val now = Instant.parse("2022-02-09T23:59:59Z")
        assertEquals(initialStockList, stock.stockList(now).valueOrNull())
    }

    @Test
    fun `updates stock if last modified yesterday`() {
        val now = Instant.parse("2022-02-10T00:00:01Z")
        val expectedUpdatedResult = StockList(
            lastModified = now,
            items = listOf(
                item("banana", oct29.minusDays(1), 41),
                item("kumquat", oct29.plusDays(1), 100)
            )
        )
        assertEquals(expectedUpdatedResult, stock.stockList(now).valueOrNull())
        assertEquals(expectedUpdatedResult, items.load().valueOrNull())
    }

    @Test
    fun `updates stock by two days if last modified the day before yesterday`() {
        val now = Instant.parse("2022-02-11T00:00:01Z")
        val expectedUpdatedResult = StockList(
            lastModified = now,
            items = listOf(
                item("banana", oct29.minusDays(1), 40),
                item("kumquat", oct29.plusDays(1), 99)
            )
        )
        assertEquals(expectedUpdatedResult, stock.stockList(now).valueOrNull())
        assertEquals(expectedUpdatedResult, items.load().valueOrNull())
    }

    @Test
    fun `does not update stock if modified tomorrow`() {
        val now = Instant.parse("2022-02-08T00:00:01Z")
        assertEquals(initialStockList, stock.stockList(now).valueOrNull())
        assertEquals(initialStockList, items.load().valueOrNull())
    }

    @Test
    fun `parallel execution`() {
        val count = 8
        val executor = Executors.newFixedThreadPool(count)
        val barrier = CyclicBarrier(count)
        val futures = executor.invokeAll(
            (1..count).map {
                Callable {
                    barrier.await()
                    `updates stock if last modified yesterday`()
                }
            }
        )
        futures.forEach { it.get() }
    }

    @Test
    fun `sanity check`() {
        for (i in 1..10) {
            `parallel execution`()
        }
    }
}
