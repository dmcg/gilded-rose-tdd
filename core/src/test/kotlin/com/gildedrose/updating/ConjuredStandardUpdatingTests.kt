package com.gildedrose.updating

import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConjuredStandardUpdatingTests {

    @Test fun `items decrease in quality two per day`() {
        assertEquals(
            item("Conjured banana", oct29, 40),
            item("Conjured banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("Conjured banana", oct29, 42),
            item("Conjured banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            item("Conjured banana", oct29, 38),
            item("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            item("Conjured banana", oct29, 0),
            item("Conjured banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("Conjured banana", oct29, 0),
            item("Conjured banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality four per day after sell by date`() {
        assertEquals(
            item("Conjured banana", oct29, 38),
            item("Conjured banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            item("Conjured banana", oct29, 42),
            item("Conjured banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            item("Conjured banana", oct29, 34),
            item("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            item("Conjured banana", oct29, 36),
            item("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            item("Conjured banana", null, 42),
            item("Conjured banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            item("Conjured banana", oct29, 50),
            item("Conjured banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("Conjured banana", oct29, 49),
            item("Conjured banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("Conjured banana", oct29, 47),
            item("Conjured banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
