package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.item
import com.gildedrose.oct29
import com.gildedrose.persistence.StockListLoadingError.*
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant

class PersistenceTests {

    private val now = Instant.now()
    private val items = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29.plusDays(1), 101),
        item("undated", null, 50)
    )

    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stockList = StockList(now, items)
        stockList.saveTo(file)
        assertEquals(
            Success(stockList),
            file.loadItems()
        )
    }

    @Test
    fun `save and load empty stockList`() {
        val stockList = StockList(now, emptyList())
        assertEquals(
            Success(stockList),
            stockList.toLines().toStockList()
        )
    }

    @Test
    fun `load from empty file`() {
        assertEquals(
            Success(StockList(Instant.EPOCH, emptyList())),
            emptySequence<String>().toStockList()
        )
    }

    @Test
    fun `load with no LastModified header`() {
        val lines = sequenceOf("# Banana")
        assertEquals(
            Success(StockList(Instant.EPOCH, emptyList())),
            lines.toStockList()
        )
    }

    @Test
    fun `fails load with blank LastModified header`() {
        assertEquals(
            Failure(CouldntParseLastModified("Could not parse LastModified header: Text '' could not be parsed at index 0")),
            sequenceOf("# LastModified:").toStockList()
        )
    }

    @Test
    fun `fails to load with negative quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\t-1")),
            sequenceOf("id\tbanana\t2022-07-08\t-1").toStockList()
        )
    }

    @Test
    fun `fails to load with blank id`() {
        assertEquals(
            Failure(BlankID("\tbanana\t2022-07-08\t42")),
            sequenceOf("\tbanana\t2022-07-08\t42").toStockList()
        )
    }

    @Disabled
    @Test
    fun `fails to load with blank name`() {
        assertEquals(
            Failure(BlankName("id\t\t2022-07-08\t42")),
            sequenceOf("id\t\t2022-07-08\t42").toStockList()
        )
    }

    @Test
    fun `fails to load with too few fields`() {
        assertEquals(
            Failure(NotEnoughFields("id\tbanana\t2022-07-08")),
            sequenceOf("id\tbanana\t2022-07-08").toStockList()
        )
    }

    @Test
    fun `fails to load with no quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\t")),
            sequenceOf("id\tbanana\t2022-07-08\t").toStockList()
        )
    }

    @Test
    fun `fails to load with duff quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\teh?")),
            sequenceOf("id\tbanana\t2022-07-08\teh?").toStockList()
        )
    }

    @Test
    fun `fails to load with bad sell by`() {
        assertEquals(
            Failure(CouldntParseSellBy("id\tbanana\teh?\t42")),
            sequenceOf("id\tbanana\teh?\t42").toStockList()
        )
    }
}


