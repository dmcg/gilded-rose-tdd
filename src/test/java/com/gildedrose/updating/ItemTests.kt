package com.gildedrose.updating

import com.gildedrose.Item
import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ItemTests {

    @Test fun `no item should have its quality raised above 50 by updating`() {
        val testItem = Item("banana", null, 50)
        assertEquals(
            Item("banana", null, 50),
            testItem.copy(quality = add(testItem.quality, 1))
        )
    }

    @Test fun `no item should have its quality reduced below 0 by updating`() {
        val testItem = Item("banana", null, 2)
        assertEquals(
            Item("banana", null, 0),
            testItem.copy(quality = subtract(testItem.quality, 3))
        )
    }

    @Test fun `items can keep a quality of above 50`() {
        val testItem = Item("banana", null, 55)
        assertEquals(
            Item("banana", null, 54),
            testItem.copy(quality = subtract(testItem.quality, 1))
        )
        val testItem1 = Item("banana", null, 55)
        assertEquals(
            Item("banana", null, 55),
            testItem1.copy(quality = subtract(testItem1.quality, -1))
        )
    }

    @Test fun `item types for equality`() {
        assertNotEquals(
            Item("Conjured banana", oct29, 50),
            Item("Conjured Aged Brie", oct29, 50).copy(name = "Conjured banana")
        )
    }
}
