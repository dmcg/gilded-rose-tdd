package com.gildedrose.domain

import com.gildedrose.item
import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConjuredAgedBrieUpdatingTests {

    @Test fun `increases in quality by two every day until its sell by date`() {
        assertEquals(
            item("Conjured Aged Brie", oct29, 44),
            item("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `increases in quality by four every day after its sell by date`() {
        assertEquals(
            item("Conjured Aged Brie", oct29, 46),
            item("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `doesn't get better than 50`() {
        assertEquals(
            item("Conjured Aged Brie", oct29, 50),
            item("Conjured Aged Brie", oct29, 50).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            item("Conjured Aged Brie", oct29, 50),
            item("Conjured Aged Brie", oct29, 49).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
