package com.gildedrose.domain

import com.gildedrose.oct29
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConjuredAgedBrieUpdatingTests {

    @Test fun `increases in quality by two every day until its sell by date`() {
        assertEquals(
            testItem("Conjured Aged Brie", oct29, 44),
            testItem("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `increases in quality by four every day after its sell by date`() {
        assertEquals(
            testItem("Conjured Aged Brie", oct29, 46),
            testItem("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `doesn't get better than 50`() {
        assertEquals(
            testItem("Conjured Aged Brie", oct29, 50),
            testItem("Conjured Aged Brie", oct29, 50).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            testItem("Conjured Aged Brie", oct29, 50),
            testItem("Conjured Aged Brie", oct29, 49).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
