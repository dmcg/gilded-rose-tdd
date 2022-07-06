package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.testItem
import com.gildedrose.oct29
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
        testItem("banana", oct29, 42),
        testItem("kumquat", oct29.plusDays(1), 101),
        testItem("undated", null, 50)
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stockList = StockList(now, items)
        stockList.saveTo(file)
        assertEquals(
            stockList,
            file.loadItems()
        )
    }

    @Test
    fun `save and load empty stockList`() {
        val stockList = StockList(now, emptyList())
        assertEquals(
            stockList,
            stockList.toLines().toStockList()
        )
    }

    @Test
    fun `load from empty file`() {
        assertEquals(
            StockList(Instant.EPOCH, emptyList()),
            emptySequence<String>().toStockList()
        )
    }

    @Test
    fun `load with no LastModified header`() {
        val lines = sequenceOf("# Banana")
        assertEquals(
            StockList(Instant.EPOCH, emptyList()),
            lines.toStockList()
        )
    }

    @Test
    fun `load with blank LastModified header`() {
        val lines = sequenceOf("# LastModified:")
        try {
            lines.toStockList()
            fail("didn't throw")
        } catch (x: IOException) {
            assertEquals(
                "Could not parse LastModified header: Text '' could not be parsed at index 0",
                x.message
            )
        }
    }
}


