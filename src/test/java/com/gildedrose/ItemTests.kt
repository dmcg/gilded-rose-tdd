package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ItemTests {

    @Test fun `toString shows type`() {
        assertEquals(
            "Item(name=banana, sellByDate=2021-10-29, quality=50, type=STANDARD)",
            itemOf("banana", oct29, 50).toString()
        )
    }

    @Test fun `no item should have its quality raised above 50 by updating`() {
        assertEquals(
            itemOf("banana", null, 50),
            itemOf("banana", null, 50).withQuality(51)
        )
    }

    @Test fun `no item should have its quality reduced below 0 by updating`() {
        assertEquals(
            itemOf("banana", null, 0),
            itemOf("banana", null, 2).withQuality(-1)
        )
    }

    @Test fun `items can keep a quality of above 50`() {
        assertEquals(
            itemOf("banana", null, 54),
            itemOf("banana", null, 55).withQuality(54)
        )
        assertEquals(
            itemOf("banana", null, 55),
            itemOf("banana", null, 55).withQuality(55)
        )
    }

    @Test fun `cannot create an item with negative quality`() {
        assertThrows<IllegalArgumentException> {
            itemOf("banana", null, -1)
        }
    }
}
