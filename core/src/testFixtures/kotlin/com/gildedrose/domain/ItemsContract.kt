package com.gildedrose.domain

import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.*
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.test.assertEquals

val nullStockist = StockList(
    lastModified = Instant.EPOCH,
    items = emptyList()
)

val initialStockList = StockList(
    lastModified = Instant.parse("2022-02-09T23:59:59Z"),
    items = listOf(
        item("banana", oct29.minusDays(1), 42),
        item("kumquat", null, 101)
    )
)

abstract class ItemsContract<TX : TXContext> {

    abstract val items: Items<TX>

    @Test
    fun `returns empty stocklist before any save`() {
        items.inTransaction {
            assertEquals(
                Success(nullStockist),
                items.load()
            )
        }
    }

    @Test
    fun `returns last saved stocklist`() {
        items.inTransaction {
            items.save(initialStockList)
            assertEquals(
                Success(initialStockList),
                items.load()
            )

            val modifiedStockList = initialStockList.copy(
                lastModified = initialStockList.lastModified.plusSeconds(3600),
                items = initialStockList.items.drop(1)
            )
            items.save(modifiedStockList)
            assertEquals(
                Success(modifiedStockList),
                items.load()
            )
        }
    }

    @Test
    fun `can save an empty stocklist`() {
        items.inTransaction {
            items.save(initialStockList)
            assertEquals(
                Success(initialStockList),
                items.load()
            )

            val modifiedStockList = initialStockList.copy(
                lastModified = initialStockList.lastModified.plusSeconds(3600),
                items = emptyList()
            )
            items.save(modifiedStockList)
            assertEquals(
                Success(modifiedStockList),
                items.load()
            )
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1970-01-01T00:00:00Z",
            "2022-12-31T23:59:59Z",
            "2023-01-01T00:00:00Z",
            "2023-06-30T23:59:59Z",
            "2023-07-01T00:00:00Z"
        ]
    )

    fun `can save stockLists with lots of lastModified in lots of timezones`(candidate: String) {
        val initialTimeZone = TimeZone.getDefault()
        try {
            val stockList = initialStockList.copy(lastModified = Instant.parse(candidate))

            TimeZone.setDefault(TimeZone.getTimeZone(TimeZone.getAvailableIDs().random()))
            items.inTransaction {
                items.save(stockList)
            }
            TimeZone.setDefault(TimeZone.getTimeZone(TimeZone.getAvailableIDs().random()))
            items.inTransaction {
                assertEquals(
                    Success(stockList),
                    items.load()
                )
            }
        } finally {
            TimeZone.setDefault(initialTimeZone)
        }
    }

    open fun transactions() {
        val cyclicBarrier = CyclicBarrier(2)
        val thread = thread {
            items.inTransaction {
                items.save(initialStockList)
                cyclicBarrier.await()
                cyclicBarrier.await()
            }
        }

        cyclicBarrier.await()
        items.inTransaction {
            assertEquals(Success(nullStockist), items.load())
        }

        cyclicBarrier.await()
        thread.join()
        items.inTransaction {
            assertEquals(Success(initialStockList), items.load())
        }
    }

}
