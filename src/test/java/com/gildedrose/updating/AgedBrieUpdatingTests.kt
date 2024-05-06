package com.gildedrose.updating

import com.gildedrose.Item
import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AgedBrieUpdatingTests {

    @Test fun `increases in quality by one every day until its sell by date`() {
        assertEquals(
            Item("Aged Brie", oct29, 43),
            Item("Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `increases in quality by two every day after its sell by date`() {
        assertEquals(
            Item("Aged Brie", oct29, 44),
            Item("Aged Brie", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }

    @Test fun `doesn't get better than 50`() {
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
