package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.domain.StockListLoadingError
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import com.gildedrose.updating.StockUpdateDecision.DoNothing
import com.gildedrose.updating.StockUpdateDecision.SaveUpdate
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import java.time.Instant.parse as t

class MaybeUpdateStockTests {

    @Test
    fun `loads stock but doesn't update if loaded same day as last modified`() {
        val initialStockList = StockList(t("2021-02-09T00:00:00Z"), someItems)
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
        val initialStockList = StockList(t("2021-02-09T23:59:59Z"), someItems)
        val expectedItems = initialStockList.items.withQualityDecreasedBy(1)
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
        val initialStockList = StockList(t("2021-02-09T23:59:59Z"), someItems)
        val expectedItems = someItems.withQualityDecreasedBy(2)
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
        val initialStockList = StockList(t("2021-02-09T00:00:00Z"), someItems)
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
    @Test
    fun `updates stock if last modified yesterday`() {
        // Given
        val initialStockList = StockList(t("2021-02-09T23:59:59Z"), someItems)
        val firstInstantOfNextDay = t("2021-02-10T00:00:00Z")

        // When
        val (result, nextState) = doLoadAndUpdate(initialStockList, firstInstantOfNextDay)

        // Then
        val expectedUpdatedResult = StockList(firstInstantOfNextDay, someItems.withQualityDecreasedBy(1))
        assertAll(
            { assertEquals(expectedUpdatedResult.asSuccess(), result) },
            { assertEquals(expectedUpdatedResult, nextState) }
        )
    }

    @Test
    fun `does not update stock if last modified today`() {
        val initialStockList = StockList(t("2021-02-09T00:00:00Z"), someItems)
        val lastInstantOfSameDay = t("2021-02-09T23:59:59.9999Z")

        val (result, nextState) = doLoadAndUpdate(initialStockList, lastInstantOfSameDay)

        val expectedNotUpdatedResult = initialStockList
        assertAll(
            { assertEquals(expectedNotUpdatedResult.asSuccess(), result) },
            { assertEquals(expectedNotUpdatedResult, nextState) }
        )
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
        repeat(10) {
            `parallel execution`()
        }
    }
}

private fun List<Item>.withQualityDecreasedBy(qualityChange: Int): List<Item> =
    map { it.copy(quality = it.quality - qualityChange) }

data class DoLoadAndUpdateOutcome(
    val result: Result4k<StockList, StockListLoadingError>,
    val nextState: StockList,
)

private fun doLoadAndUpdate(
    initialStockList: StockList,
    now: Instant,
): DoLoadAndUpdateOutcome {
    val items = InMemoryItems(initialStockList)
    val stock = Stock(items, zoneId = londonZone)
    val result = items.inTransaction {
        stock.loadAndUpdateStockList(now)
    }
    val nextState = items.inTransaction {
        items.load().onFailure { error("Should not fail") }
    }
    return DoLoadAndUpdateOutcome(result, nextState)
}

private val someItems = listOf(
    item("banana", oct29, 42),
    item("kumquat", oct29, 101)
)

private val londonZone = ZoneId.of("Europe/London")
