package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdatingTests {

    val items = listOf(Item("banana", oct29, 42))

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            listOf(Item("banana", oct29, 41)),
            updateItems(items, days = 1)
        )
        assertEquals(
            items,
            updateItems(items, days = 0)
        )
        assertEquals(
            listOf(Item("banana", oct29, 40)),
            updateItems(items, days = 2)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            listOf(Item("banana", oct29, 0)),
            updateItems(listOf(Item("banana", oct29, 0)), days = 1)
        )
        assertEquals(
            listOf(Item("banana", oct29, 0)),
            updateItems(listOf(Item("banana", oct29, 1)), days = 2)
        )
    }
}
