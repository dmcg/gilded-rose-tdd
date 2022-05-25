package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConjuredAgedBrieUpdatingTests {

    @Test fun `increases in quality by two every day until its sell by date`() {
        assertEquals(
            itemOf("Conjured Aged Brie", oct29, 44),
            itemOf("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `increases in quality by four every day after its sell by date`() {
        assertEquals(
            itemOf("Conjured Aged Brie", oct29, 46),
            itemOf("Conjured Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `doesn't get better than 50`() {
        assertEquals(
            itemOf("Conjured Aged Brie", oct29, 50),
            itemOf("Conjured Aged Brie", oct29, 50).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            itemOf("Conjured Aged Brie", oct29, 50),
            itemOf("Conjured Aged Brie", oct29, 49).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
