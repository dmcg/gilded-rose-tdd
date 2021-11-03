package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StockTests {
    @Test
    fun `add item to stock`() {
        val stock = listOf<Item>()
        assertEquals(
            listOf<Item>(),
            stock
        )

        val newStock = stock + Item("banana", oct29, 42u)
        assertEquals(
            listOf(Item("banana", oct29, 42u)),
            newStock
        )
    }
}

val oct29 = LocalDate.parse("2021-10-29")
