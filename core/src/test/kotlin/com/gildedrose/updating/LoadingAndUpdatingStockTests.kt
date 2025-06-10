package com.gildedrose.updating

import com.gildedrose.domain.*
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import dev.forkhandles.result4k.Result
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
import kotlin.test.assertNull
import java.time.Instant.parse as t

class LoadingAndUpdatingStockTests {
    private val someItems = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29, 101)
    )

    @Test
    fun `updates stock if last modified yesterday`() {
        // Given
        val initialStockList = StockList(t("2021-02-09T23:59:59Z"), someItems)
        val firstInstantOfNextDay = t("2021-02-10T00:00:00Z")

        // When
        val (result, nextState, savedStockList) = doLoadAndUpdate(initialStockList, firstInstantOfNextDay)

        // Then
        val expectedUpdatedResult = StockList(firstInstantOfNextDay, someItems.withQualityDecreasedBy(1))
        assertAll(
            { assertEquals(expectedUpdatedResult.asSuccess(), result) },
            { assertEquals(expectedUpdatedResult, nextState) },
            { assertEquals(expectedUpdatedResult, savedStockList) }
        )
    }

    @Test
    fun `does not update stock if last modified today`() {
        val initialStockList = StockList(t("2021-02-09T00:00:00Z"), someItems)
        val lastInstantOfSameDay = t("2021-02-09T23:59:59.9999Z")

        val (result, nextState, savedStockList) = doLoadAndUpdate(initialStockList, lastInstantOfSameDay)

        val expectedNotUpdatedResult = initialStockList
        assertAll(
            { assertEquals(expectedNotUpdatedResult.asSuccess(), result) },
            { assertEquals(expectedNotUpdatedResult, nextState) },
            { assertNull(savedStockList) }
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

private data class DoLoadAndUpdateOutcome(
    val result: Result4k<StockList, StockListLoadingError>,
    val nextState: StockList,
    val savedStockList: StockList? = null
)

private fun doLoadAndUpdate(
    initialStockList: StockList,
    now: Instant,
): DoLoadAndUpdateOutcome {
    val inMemoryItems = InMemoryItems(initialStockList)
    val sensingItems = object : Items<NoTX> by inMemoryItems {
        var savedStockList: StockList? = null
        context(NoTX)
        override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IOError> {
            return inMemoryItems.save(stockList).also {
                savedStockList = stockList
            }
        }
    }
    val stock = Stock(sensingItems, zoneId = londonZone)
    val result = sensingItems.inTransaction {
        stock.loadAndUpdateStockList(now)
    }
    val nextState = sensingItems.inTransaction {
        inMemoryItems.load().onFailure { error("Should not fail") }
    }
    return DoLoadAndUpdateOutcome(result, nextState, sensingItems.savedStockList)
}

internal fun List<Item>.withQualityDecreasedBy(qualityChange: Int): List<Item> =
    map { it.copy(quality = it.quality - qualityChange) }

private val londonZone = ZoneId.of("Europe/London")
