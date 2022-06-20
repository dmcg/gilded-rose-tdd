package com.gildedrose.domain

import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConjuredStandardUpdatingTests {

    @Test fun `items decrease in quality two per day`() {
        assertEquals(
            itemOf("Conjured banana", oct29, 40),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 42),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 38),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            itemOf("Conjured banana", oct29, 0),
            itemOf("Conjured banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 0),
            itemOf("Conjured banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality four per day after sell by date`() {
        assertEquals(
            itemOf("Conjured banana", oct29, 38),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 42),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 34),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 36),
            itemOf("Conjured banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            itemOf("Conjured banana", null, 42),
            itemOf("Conjured banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            itemOf("Conjured banana", oct29, 50),
            itemOf("Conjured banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 49),
            itemOf("Conjured banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("Conjured banana", oct29, 47),
            itemOf("Conjured banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
