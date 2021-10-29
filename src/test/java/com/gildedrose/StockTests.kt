package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StockTests {

    private val sellBy = LocalDate.parse("2021-10-29")

    @Test
    fun `add item to stock`() {
        val stock = listOf<Item>()
        assertEquals(
            listOf<Item>(),
            stock
        )

        val newStock = stock + Item("banana", sellBy, 42u)
        assertEquals(
            listOf(Item("banana", sellBy, 42u)),
            newStock
        )
    }
}
