package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.item
import com.gildedrose.oct29
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

interface InTransaction<TX> {
    operator fun <R> invoke(block: context(TX) () -> R) : R
}

abstract class ItemsContract<TX>(
    val items: Items<TX>,
    val inTransaction: InTransaction<TX>
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
        inTransaction {
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
        inTransaction {
            items.save(initialStockList)
        }
        assertEquals(
            Success(initialStockList),
            inTransaction { items.load() }
        )

        val modifiedStockList = initialStockList.copy(
            lastModified = initialStockList.lastModified.plusSeconds(3600),
            items = initialStockList.items.drop(1)
        )
        inTransaction {
            items.save(modifiedStockList)
            assertEquals(
                Success(modifiedStockList),
                items.load()
            )
        }
    }
}
