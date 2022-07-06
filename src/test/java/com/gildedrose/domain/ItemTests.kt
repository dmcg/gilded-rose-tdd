package com.gildedrose.domain

import com.gildedrose.oct29
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ItemTests {

    @Test fun `toString shows type`() {
        assertEquals(
            "Item(name=banana, sellByDate=2021-10-29, quality=50, type=STANDARD)",
            testItem("banana", oct29, 50).toString()
        )
    }

    @Test fun `no item should have its quality raised above 50 by updating`() {
        assertEquals(
            testItem("banana", null, 50),
            testItem("banana", null, 50).withQuality(51)
        )
    }

    @Test fun `no item should have its quality reduced below 0 by updating`() {
        assertEquals(
            testItem("banana", null, 0),
            testItem("banana", null, 2).withQuality(-1)
        )
    }

    @Test fun `items can keep a quality of above 50`() {
        assertEquals(
            testItem("banana", null, 54),
            testItem("banana", null, 55).withQuality(54)
        )
        assertEquals(
            testItem("banana", null, 55),
            testItem("banana", null, 55).withQuality(55)
        )
    }

    @Test fun `cannot create an item with negative quality`() {
        assertNull(Item("banana", null, -1))
    }

    @Test fun `item types for equality`() {
        assertNotEquals(
            testItem("Conjured banana", oct29, 50),
            testItem("Conjured Aged Brie", oct29, 50).copy(name = "Conjured banana")
        )
    }

    @Test fun `item types for toString`() {
        assertEquals("Item(name=Conjured banana, sellByDate=2021-10-29, quality=50, type=CONJURED STANDARD)",
            testItem("Conjured banana", oct29, 50).toString()
        )
    }

}
