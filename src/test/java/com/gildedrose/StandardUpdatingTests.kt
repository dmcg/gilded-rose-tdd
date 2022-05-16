package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StandardUpdatingTests {

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            itemOf("banana", oct29, 41),
            itemOf("banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("banana", oct29, 42),
            itemOf("banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            itemOf("banana", oct29, 40),
            itemOf("banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            itemOf("banana", oct29, 0),
            itemOf("banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("banana", oct29, 0),
            itemOf("banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality two per day after sell by date`() {
        assertEquals(
            itemOf("banana", oct29, 40),
            itemOf("banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            itemOf("banana", oct29, 42),
            itemOf("banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            itemOf("banana", oct29, 38),
            itemOf("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            itemOf("banana", oct29, 39),
            itemOf("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            itemOf("banana", null, 42),
            itemOf("banana", null, 42).updatedBy(days = 1, on = oct29)
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

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            itemOf("banana", oct29, 51),
            itemOf("banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("banana", oct29, 50),
            itemOf("banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("banana", oct29, 49),
            itemOf("banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `cannot create an item with negative quality`() {
        assertThrows<IllegalArgumentException> {
            itemOf("banana", null, -1)
        }
    }
}
