package com.gildedrose.updating

import com.gildedrose.domain.StockList
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import com.gildedrose.updating.StockUpdateDecision.DoNothing
import com.gildedrose.updating.StockUpdateDecision.SaveUpdate
import dev.forkhandles.result4k.asSuccess
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import java.time.Instant.parse as t

class MaybeUpdateStockTests {

    @Test
    fun `loads stock but doesn't update if loaded same day as last modified`() {
        val initialStockList = StockList(
            lastModified = t("2021-02-09T00:00:00Z"),
            items = listOf(
                item("banana", oct29.minusDays(1), 42),
                item("kumquat", oct29.plusDays(1), 101)
            )
        )
        assertEquals(
            DoNothing(initialStockList),
            mayBeUpdate(initialStockList, t("2021-02-09T00:00:00Z"), londonZone)
        )
        assertEquals(
            DoNothing(initialStockList),
            mayBeUpdate(initialStockList, t("2021-02-09T23:59:59.99999Z"), londonZone)
        )
    }

    @Test
    fun `updates stock if last modified yesterday`() {
        val initialStockList = StockList(
            lastModified = t("2021-02-09T23:59:59Z"),
            items = listOf(
                item("banana", oct29.minusDays(1), 42),
                item("kumquat", oct29.plusDays(1), 101)
            )
        )
        val expectedItems = initialStockList.items.map { it.copy(quality = it.quality - 1) }
        assertEquals(
            SaveUpdate(
                StockList(t("2021-02-10T00:00:00Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, t("2021-02-10T00:00:00Z"), londonZone)
        )
        assertEquals(
            SaveUpdate(
                StockList(t("2021-02-10T23:59:59.9999Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, t("2021-02-10T23:59:59.9999Z"), londonZone)
        )
    }

    @Test
    fun `updates stock by two days if last modified the day before yesterday`() {
        val initialStockList = StockList(
            lastModified = t("2021-02-09T23:59:59Z"),
            items = listOf(
                item("banana", oct29.minusDays(1), 42),
                item("kumquat", oct29.plusDays(1), 101)
            )
        )
        val expectedItems = initialStockList.items.map { it.copy(quality = it.quality - 2) }
        assertEquals(
            SaveUpdate(
                StockList(t("2021-02-11T00:00:00Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, t("2021-02-11T00:00:00Z"), londonZone)
        )
        assertEquals(
            SaveUpdate(
                StockList(t("2021-02-11T23:59:59.9999Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, t("2021-02-11T23:59:59.9999Z"), londonZone)
        )
    }

    @Test
    fun `does not update stock if modified tomorrow`() {
        val initialStockList = StockList(
            lastModified = t("2021-02-09T00:00:00Z"),
            items = listOf(
                item("banana", oct29.minusDays(1), 42),
                item("kumquat", oct29.plusDays(1), 101)
            )
        )
        assertEquals(
            DoNothing(initialStockList),
            mayBeUpdate(initialStockList, t("2021-02-08T00:00:00Z"), londonZone)
        )
        assertEquals(
            DoNothing(initialStockList),
            mayBeUpdate(initialStockList, t("2021-02-08T23:59:59.99999Z"), londonZone)
        )
    }
}

class LoadingAndUpdatingStockTests {

    private val initialStockList = StockList(
        lastModified = t("2021-02-09T23:59:59Z"),
        items = listOf(
            item("banana", oct29.minusDays(1), 42),
            item("kumquat", oct29.plusDays(1), 101)
        )
    )
    private val items = InMemoryItems(initialStockList)
    private val stock = Stock(items, zoneId = londonZone)

    @Test
    fun `updates stock if last modified yesterday`() {
        val now = t("2021-02-10T00:00:01Z")
        val expectedUpdatedResult = StockList(
            lastModified = now,
            items = initialStockList.items.map { it.copy(quality = it.quality - 1) }
        ).asSuccess()
        items.inTransaction {
            assertEquals(
                expectedUpdatedResult,
                stock.loadAndUpdateStockList(now)
            )
        }
        items.inTransaction {
            assertEquals(
                expectedUpdatedResult,
                items.load()
            )
        }
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

val londonZone = ZoneId.of("Europe/London")
