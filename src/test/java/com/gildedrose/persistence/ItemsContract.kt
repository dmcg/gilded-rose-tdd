package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.item
import com.gildedrose.oct29
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

abstract class ItemsContract<TX>(
    val items: Items<TX>,
    val inTransaction: (block: context(TX) () -> Unit) -> Unit
) {
    private val initialStockList = StockList(
        lastModified = Instant.parse("2022-02-09T23:59:59Z"),
        items = listOf(
            item("banana", oct29.minusDays(1), 42),
            item("kumquat", oct29.plusDays(1), 101)
        )
    )

    @Test
    fun `returns empty stocklist before any save`() {
        items.inTransaction {
            assertEquals(
                Success(
                    StockList(
                        lastModified = Instant.EPOCH,
                        items = emptyList()
                    )
                ),
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
    fun readerMonad() {
        val saver = items.saveToo(initialStockList)
        items.inTransaction {
            saver.runInContext()
        }

        items.inTransaction {
            assertEquals(
                Success(initialStockList),
                items.load()
            )
        }

        val updated = items.inTransaction {
            items.loadToo().flatMapResult { stockList: StockList ->
                items.saveToo(
                    stockList.withLastModified(
                        stockList.lastModified.plusSeconds(3600)
                    )
                )
            }.runInContext()
        }
        val expected = Success(
            initialStockList.withLastModified(
                initialStockList.lastModified.plusSeconds(3600)
            )
        )
        assertEquals(expected, updated)
        items.inTransaction {
            assertEquals(expected, items.load())
        }
    }
}

private fun StockList.withLastModified(instant: Instant) =
    copy(lastModified = instant)
