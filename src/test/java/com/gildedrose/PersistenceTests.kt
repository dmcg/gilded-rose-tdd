package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PersistenceTests {

    @Test
    fun loadAndSave(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stock = listOf(
            Item("banana", oct29, 42u),
            Item("kumquat", oct29.plusDays(1), 101u)
        )
        stock.saveTo(file)
        assertEquals(stock, file.loadItems())
    }

    @Test
    fun loadAndSaveEmpty(@TempDir dir: File) {
        val file = File(dir, "stock.tsv")
        val stock = emptyList<Item>()
        stock.saveTo(file)
        assertEquals(stock, file.loadItems())
    }
}


