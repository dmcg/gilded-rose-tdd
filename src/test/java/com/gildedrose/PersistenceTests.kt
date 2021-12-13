package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.time.Instant

class PersistenceTests {

    private val now = Instant.now()
    private val items = listOf(
        Item("banana", oct29, 42u),
        Item("kumquat", oct29.plusDays(1), 101u)
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stockList = StockList(now, items)
        stockList.saveTo(file)
        assertEquals(
            stockList,
            file.loadItems(defaultLastModified = now.plusSeconds(3600))
        )
    }

    @Test
    fun `save and load empty`() {
        val stockList = StockList(now, emptyList())
        assertEquals(
            stockList,
            stockList.toLines().toStockList(defaultLastModified = now.plusSeconds(3600))
        )
    }

    @Test
    fun `load with no LastModified header`() {
        val lines = sequenceOf("# Banana")
        assertEquals(
            StockList(now, emptyList()),
            lines.toStockList(defaultLastModified = now)
        )
    }

    @Test
    fun `load with blank LastModified header`() {
        val lines = sequenceOf("# LastModified:")
        try {
            lines.toStockList(defaultLastModified = now)
            fail("didn't throw")
        } catch (x: IOException) {
            assertEquals(
                "Could not parse LastModified header: Text '' could not be parsed at index 0",
                x.message
            )
        }
    }

    @Test
    fun `load legacy file`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        items.legacySaveTo(file)
        assertEquals(
            StockList(
                now,
                items
            ), file.loadItems(defaultLastModified = now)
        )
    }

}

private fun List<Item>.legacySaveTo(file: File) {
    fun Item.toLine() = "$name\t$sellByDate\t$quality"
    file.writer().buffered().use { writer ->
        forEach { item ->
            writer.appendLine(item.toLine())
        }
    }
}


