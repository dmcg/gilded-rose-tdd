package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.item
import com.gildedrose.oct29
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

abstract class ItemsContract<TX>(
    val items: Items<TX>
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
        assertEquals(
            Success(
                StockList(
                    lastModified = Instant.EPOCH,
                    items = emptyList()
                )
            ),
            items.withTransaction { tx ->
                items.load(tx)
            }
        )
    }

    @Test
    fun `returns last saved stocklist`() {
        items.withTransaction { tx ->
            items.save(initialStockList, tx)
        }
        assertEquals(
            Success(initialStockList),
            items.withTransaction { tx ->
                items.load(tx)
            }
        )

        val modifiedStockList = initialStockList.copy(
            lastModified = initialStockList.lastModified.plusSeconds(3600),
            items = initialStockList.items.drop(1)
        )
        items.withTransaction { tx ->
            items.save(modifiedStockList, tx)
            assertEquals(
                Success(modifiedStockList),
                items.load(tx)
            )
        }
    }
}
