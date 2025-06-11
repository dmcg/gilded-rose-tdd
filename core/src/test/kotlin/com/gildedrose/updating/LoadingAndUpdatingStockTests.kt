package com.gildedrose.updating

import com.gildedrose.domain.*
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import dev.forkhandles.result4k.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import java.time.Instant.parse as t

class LoadingAndUpdatingStockTests {
    private val someItems = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29, 101)
    )

    @Test
    fun `updates stock if last modified yesterday`() {
        val lastModified = t("2021-02-09T23:59:59Z")
        val firstInstantOfNextDay = t("2021-02-10T00:00:00Z")

        Given(StockList(lastModified, someItems), now = firstInstantOfNextDay)
            .When {
                doLoadAndUpdate()
            }
            .Then { outcome ->
                val expectedUpdatedResult = StockList(firstInstantOfNextDay, someItems.withQualityDecreasedBy(1))
                assertEquals(
                    Outcome(
                        result = expectedUpdatedResult.asSuccess(),
                        savedState = expectedUpdatedResult,
                        savedStockList = expectedUpdatedResult
                    ),
                    outcome
                )
            }
    }


    @Test
    fun `does not update stock if last modified today`() {
        val lastModified = t("2021-02-09T00:00:00Z")
        val lastInstantOfSameDay = t("2021-02-09T23:59:59.9999Z")
        val initialStockList = StockList(lastModified, someItems)
        Given(initialStockList, now = lastInstantOfSameDay)
            .When {
                doLoadAndUpdate()
            }
            .Then { outcome ->
                val expectedNotUpdatedResult = initialStockList
                assertEquals(
                    Outcome(
                        result = expectedNotUpdatedResult.asSuccess(),
                        savedState = expectedNotUpdatedResult,
                        savedStockList = null
                    ),
                    outcome
                )
            }
    }

    @Test
    fun `returns error in loading stock`() {
        val lastModified = t("2021-02-09T00:00:00Z")
        val lastInstantOfSameDay = t("2021-02-09T23:59:59.9999Z")
        val initialStockList = StockList(lastModified, someItems)
        val loadingError = StockListLoadingError.IOError("deliberate")
        Given(initialStockList, lastInstantOfSameDay, loadingError)
            .When {
                doLoadAndUpdate()
            }
            .Then { outcome ->
                assertEquals(
                    Outcome(
                        result = loadingError.asFailure(),
                        savedState = initialStockList,
                        savedStockList = null
                    ),
                    outcome
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
        repeat(10) {
            `parallel execution`()
        }
    }
}

private data class Given(
    val initialStockList: StockList,
    val now: Instant,
    val loadingError: StockListLoadingError? = null,
) {
    fun doLoadAndUpdate(): Outcome {
        val inMemoryItems = InMemoryItems(initialStockList)
        val sensingItems = object : Items<NoTX> by inMemoryItems {
            var savedStockList: StockList? = null

            context(NoTX)
            override fun load(): Result<StockList, StockListLoadingError> {
                return loadingError?.asFailure() ?: inMemoryItems.load()
            }

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
        return Outcome(result, nextState, sensingItems.savedStockList)
    }

}

private data class Outcome(
    val result: Result4k<StockList, StockListLoadingError>,
    val savedState: StockList,
    val savedStockList: StockList? = null,
)

internal fun List<Item>.withQualityDecreasedBy(qualityChange: Int): List<Item> =
    map { it.copy(quality = it.quality - qualityChange) }

private val londonZone = ZoneId.of("Europe/London")

private inline fun <T, R> T.When(block: (T).(T) -> R): R = block(this)
private inline fun <T, R> T.Then(block: (T) -> R): R = this.let(block)

