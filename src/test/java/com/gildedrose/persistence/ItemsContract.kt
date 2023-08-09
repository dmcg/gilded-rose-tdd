package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.item
import com.gildedrose.oct29
import com.gildedrose.testing.IOResolver
import com.gildedrose.testing.assertSucceeds
import com.gildedrose.testing.assertSucceedsWith
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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

context(IO)
@ExtendWith(IOResolver::class)
abstract class ItemsContract<TX : TXContext> {

    abstract val items: Items<TX>

    @Test
    fun `returns empty stocklist before any save`() {
        assertEquals(
            Success(nullStockist),
            items.transactionally { load() }
        )
    }

    @Test
    fun `returns last saved stocklist`() {
        val modifiedStockList = initialStockList.copy(
            lastModified = initialStockList.lastModified.plusSeconds(3600),
            items = initialStockList.items.drop(1)
        )
        checkStocklistSaving(initialStockList, modifiedStockList)
    }

    @Test
    fun `can save an empty stocklist`() {
        val modifiedStockList = StockList(
            lastModified = initialStockList.lastModified.plusSeconds(3600),
            items = emptyList()
        )
        checkStocklistSaving(initialStockList, modifiedStockList)
    }

    private fun checkStocklistSaving(firstStockList: StockList, secondStockList: StockList) {
        items.inTransaction {
            assertSucceeds { items.save(firstStockList) }
            assertSucceedsWith(firstStockList) {
                items.load()
            }
            assertSucceeds { items.save(secondStockList) }
            assertSucceedsWith(secondStockList) {
                items.load()
            }
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
            items.transactionally { save(stockList) }
            TimeZone.setDefault(TimeZone.getTimeZone(TimeZone.getAvailableIDs().random()))
            assertEquals(
                Success(stockList),
                items.transactionally { load() }
            )
        } finally {
            TimeZone.setDefault(initialTimeZone)
        }
    }

    open fun transactions() {
        val cyclicBarrier = CyclicBarrier(2)
        val thread = thread {
            items.transactionally {
                save(initialStockList)
                cyclicBarrier.await()
                cyclicBarrier.await()
            }
        }

        cyclicBarrier.await()
        assertEquals(Success(nullStockist), items.transactionally { load() })

        cyclicBarrier.await()
        thread.join()
        assertEquals(Success(initialStockList), items.transactionally { load() })
    }

}
