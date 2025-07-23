package com.gildedrose.updating

import com.gildedrose.domain.*
import com.gildedrose.testing.Given
import com.gildedrose.testing.InMemoryItems
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import dev.forkhandles.result4k.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import java.time.Instant.parse as t

class LoadingAndUpdatingStockTests {
    private val someItems = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29, 101)
    )

    @Test
    fun `updates stock if last modified yesterday`() {
        Given(
            Fixture(
                StockList(t("2021-02-09T23:59:59Z"), someItems),
                now = t("2021-02-10T00:00:00Z")
            )
        ).When {
            loadAndUpdate()
        }.Then { outcome ->
            val expectedUpdatedResult = StockList(now, initialStockList.items.withQualityDecreasedBy(1))
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
        Given(
            Fixture(
                StockList(t("2021-02-09T00:00:00Z"), someItems),
                now = t("2021-02-09T23:59:59.9999Z")
            )
        ).When {
            loadAndUpdate()
        }.Then { outcome ->
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
    fun `load before and after midnight`() {
        Given(
            Fixture(
                StockList(t("2021-02-09T00:00:00Z"), someItems),
                now = t("2021-02-09T23:59:59.9999Z")
            )
        ).When {
            loadAndUpdate()
        }.Then { outcome ->
            val expectedNotUpdatedResult = initialStockList
            assertEquals(
                Outcome(
                    result = expectedNotUpdatedResult.asSuccess(),
                    savedState = expectedNotUpdatedResult,
                    savedStockList = null
                ),
                outcome
            )
        }.When {
            now = t("2021-02-10T00:00:00Z")
            loadAndUpdate()
        }.Then {outcome ->
            val expectedUpdatedResult = StockList(now, initialStockList.items.withQualityDecreasedBy(1))
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
    fun `returns error in loading stock`() {
        Given(
            Fixture(
                initialStockList = StockList(t("2021-02-09T00:00:00Z"), someItems),
                now = t("2021-02-09T00:00:00Z"),
                loadingError = StockListLoadingError.IOError("deliberate")
            )
        ).When {
            loadAndUpdate()
        }.Then { outcome ->
            assertEquals(
                Outcome(
                    result = loadingError!!.asFailure(),
                    savedState = initialStockList,
                    savedStockList = null
                ),
                outcome
            )
        }
    }
}

private data class Fixture(
    val initialStockList: StockList,
    var now: Instant,
    val loadingError: StockListLoadingError? = null,
) {
    fun loadAndUpdate(): Outcome {
        val inMemoryItems = InMemoryItems(initialStockList)
        val sensingItems = object : Items<NoTX> by inMemoryItems {
            var savedStockList: StockList? = null

            context(_: NoTX)
            override fun load(): Result<StockList, StockListLoadingError> {
                return loadingError?.asFailure() ?: inMemoryItems.load()
            }

            context(_:  NoTX)
            override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IOError> {
                return inMemoryItems.save(stockList).also {
                    savedStockList = stockList
                }
            }
        }
        val stock = Stock(sensingItems, zoneId = ZoneId.of("Europe/London"))
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
