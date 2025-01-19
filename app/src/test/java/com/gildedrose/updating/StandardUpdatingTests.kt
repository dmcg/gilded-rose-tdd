package com.gildedrose.updating

import com.gildedrose.item
import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StandardUpdatingTests {

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            item("banana", oct29, 41),
            item("banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("banana", oct29, 42),
            item("banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            item("banana", oct29, 40),
            item("banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            item("banana", oct29, 0),
            item("banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("banana", oct29, 0),
            item("banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality two per day after sell by date`() {
        assertEquals(
            item("banana", oct29, 40),
            item("banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            item("banana", oct29, 42),
            item("banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            item("banana", oct29, 38),
            item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            item("banana", oct29, 39),
            item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            item("banana", null, 42),
            item("banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            item("banana", oct29, 51),
            item("banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("banana", oct29, 50),
            item("banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("banana", oct29, 49),
            item("banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
