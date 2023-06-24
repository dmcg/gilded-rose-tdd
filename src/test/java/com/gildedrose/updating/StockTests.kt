package com.gildedrose.updating

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.item
import com.gildedrose.oct29
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.valueOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

@ExtendWith(IOResolver::class)
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

    context(IO)
    @Test
    fun `loads stock from file`() {
        val now = Instant.parse("2022-02-09T23:59:59Z")
        assertEquals(initialStockList, stock.loadAndUpdateStockList(now).valueOrNull())
    }

    context(IO)
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
        assertEquals(expectedUpdatedResult, stock.loadAndUpdateStockList(now).valueOrNull())
        items.inTransaction {
            assertEquals(expectedUpdatedResult, items.load().valueOrNull())
        }
    }

    context(IO)
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
        assertEquals(expectedUpdatedResult, stock.loadAndUpdateStockList(now).valueOrNull())
        items.inTransaction {
            assertEquals(expectedUpdatedResult, items.load().valueOrNull())
        }
    }

    context(IO)
    @Test
    fun `does not update stock if modified tomorrow`() {
        val now = Instant.parse("2022-02-08T00:00:01Z")
        assertEquals(initialStockList, stock.loadAndUpdateStockList(now).valueOrNull())
        items.inTransaction {
            assertEquals(initialStockList, items.load().valueOrNull())
        }
    }

    context(IO)
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

    context(IO)
    @Test
    fun `sanity check`() {
        for (i in 1..10) {
            `parallel execution`()
        }
    }
}
