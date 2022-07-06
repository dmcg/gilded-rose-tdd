package com.gildedrose.domain

import com.gildedrose.oct29
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StandardUpdatingTests {

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            testItem("banana", oct29, 41),
            testItem("banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            testItem("banana", oct29, 42),
            testItem("banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            testItem("banana", oct29, 40),
            testItem("banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            testItem("banana", oct29, 0),
            testItem("banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            testItem("banana", oct29, 0),
            testItem("banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality two per day after sell by date`() {
        assertEquals(
            testItem("banana", oct29, 40),
            testItem("banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            testItem("banana", oct29, 42),
            testItem("banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            testItem("banana", oct29, 38),
            testItem("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            testItem("banana", oct29, 39),
            testItem("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            testItem("banana", null, 42),
            testItem("banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            testItem("banana", oct29, 51),
            testItem("banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            testItem("banana", oct29, 50),
            testItem("banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            testItem("banana", oct29, 49),
            testItem("banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
