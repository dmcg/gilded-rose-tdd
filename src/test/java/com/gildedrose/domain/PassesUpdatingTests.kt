package com.gildedrose.domain

import com.gildedrose.oct29
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PassesUpdatingTests {

    @Test fun `increases in quality by one every day until 10 days to sell by date`() {
        assertEquals(
            testItem("Backstage Passes", oct29, 43),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.minusDays(10))
        )
    }

    @Test fun `increases in quality by two every day from 10 to 5 days to sell by date`() {
        assertEquals(
            testItem("Backstage Passes", oct29, 44),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.minusDays(9))
        )
        assertEquals(
            testItem("Backstage Passes", oct29, 44),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.minusDays(5))
        )
    }

    @Test fun `increases in quality by three every day from 5 days to sell by date`() {
        assertEquals(
            testItem("Backstage Passes", oct29, 45),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.minusDays(4))
        )
        assertEquals(
            testItem("Backstage Passes", oct29, 45),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `degrades completely after the sell by date`() {
        assertEquals(
            testItem("Backstage Passes", oct29, 0),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            testItem("Backstage Passes", oct29, 0),
            testItem("Backstage Passes", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(2))
        )
    }

    @Test fun `doesn't get better than 50`() {
        assertEquals(
            testItem("Backstage Passes", oct29, 50),
            testItem("Backstage Passes", oct29, 49).updatedBy(days = 1, on = oct29)
        )
    }
}
