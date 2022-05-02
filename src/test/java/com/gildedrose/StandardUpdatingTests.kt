package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}
