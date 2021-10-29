package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class Tests {

    @Test
    fun test() {
        val stock = listOf<Item>()
        assertEquals(
            listOf<Item>(),
            stock
        )

        val newStock = stock + Item("banana", LocalDate.now(), 42u)
        assertEquals(
            listOf(Item("banana", LocalDate.now(), 42u)),
            newStock
        )
    }
}
