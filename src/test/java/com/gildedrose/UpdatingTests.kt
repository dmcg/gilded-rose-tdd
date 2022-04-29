package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdatingTests {

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            Item("banana", oct29, 41),
            Item("banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 42),
            Item("banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 40),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            Item("banana", oct29, 0),
            Item("banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 0),
            Item("banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality two per day after sell by date`() {
        assertEquals(
            Item("banana", oct29, 40),
            Item("banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            Item("banana", oct29, 42),
            Item("banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            Item("banana", oct29, 38),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            Item("banana", oct29, 39),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            Item("banana", null, 42),
            Item("banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `Aged Brie increase in quality by one every day until its sell by date`() {
        assertEquals(
            Item("Aged Brie", oct29, 43),
            Item("Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `Aged Brie increase in quality by two every day after its sell by date`() {
        assertEquals(
            Item("Aged Brie", oct29, 44),
            Item("Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `Aged Brie doesn't get better than 50`() {
        assertEquals(
            Item("Aged Brie", oct29, 50),
            Item("Aged Brie", oct29, 50).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("Aged Brie", oct29, 50),
            Item("Aged Brie", oct29, 49).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
