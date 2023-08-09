package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.result4k
import com.gildedrose.item
import com.gildedrose.oct29
import com.gildedrose.persistence.StockListLoadingError.*
import com.gildedrose.testing.IOResolver
import com.gildedrose.testing.assertSucceeds
import com.gildedrose.testing.assertSucceedsWith
import dev.forkhandles.result4k.Failure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant

@ExtendWith(IOResolver::class)
class PersistenceTests {

    private val now = Instant.now()
    private val items = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29.plusDays(1), 101),
        item("undated", null, 50)
    )

    context(IO)
    @Test
    fun `save and load`(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stockList = StockList(now, items)
        assertSucceeds { stockList.saveTo(file) }
        assertSucceedsWith(
            stockList) {
            file.loadItems()
        }
    }

    @Test
    fun `save and load empty stockList`() {
        val stockList = StockList(now, emptyList())
        assertSucceedsWith(stockList) {
            stockList.toLines().toStockList()
        }
    }

    @Test
    fun `load from empty file`() {
        assertSucceedsWith(
            StockList(Instant.EPOCH, emptyList())) {
            emptySequence<String>().toStockList()
        }
    }

    @Test
    fun `load with no LastModified header`() {
        val lines = sequenceOf("# Banana")
        assertSucceedsWith(
            StockList(Instant.EPOCH, emptyList())) {
            lines.toStockList()
        }
    }

    @Test
    fun `fails load with blank LastModified header`() {
        assertEquals(
            Failure(CouldntParseLastModified("Could not parse LastModified header: Text '' could not be parsed at index 0")),
            result4k { sequenceOf("# LastModified:").toStockList() }
        )
    }

    @Test
    fun `fails to load with negative quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\t-1")),
            result4k { sequenceOf("id\tbanana\t2022-07-08\t-1").toStockList() }
        )
    }

    @Test
    fun `fails to load with blank id`() {
        assertEquals(
            Failure(BlankID("\tbanana\t2022-07-08\t42")),
            result4k { sequenceOf("\tbanana\t2022-07-08\t42").toStockList() }
        )
    }

    @Test
    fun `fails to load with blank name`() {
        assertEquals(
            Failure(BlankName("id\t\t2022-07-08\t42")),
            result4k { sequenceOf("id\t\t2022-07-08\t42").toStockList() }
        )
    }

    @Test
    fun `fails to load with too few fields`() {
        assertEquals(
            Failure(NotEnoughFields("id\tbanana\t2022-07-08")),
            result4k { sequenceOf("id\tbanana\t2022-07-08").toStockList() }
        )
    }

    @Test
    fun `fails to load with no quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\t")),
            result4k { sequenceOf("id\tbanana\t2022-07-08\t").toStockList() }
        )
    }

    @Test
    fun `fails to load with duff quality`() {
        assertEquals(
            Failure(CouldntParseQuality("id\tbanana\t2022-07-08\teh?")),
            result4k { sequenceOf("id\tbanana\t2022-07-08\teh?").toStockList() }
        )
    }

    @Test
    fun `fails to load with bad sell by`() {
        assertEquals(
            Failure(CouldntParseSellBy("id\tbanana\teh?\t42")),
            result4k { sequenceOf("id\tbanana\teh?\t42").toStockList() }
        )
    }
}


