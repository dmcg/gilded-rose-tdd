package com.gildedrose

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class UpdateStockTests {

    private val stockList = standardStockList.copy(
        lastModified = Instant.parse("2022-02-09T12:00:00Z")
    )

    @Test
    fun `doesn't update when lastModified is today`() {
        val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
        with(Fixture(standardStockList, now = sameDayAsLastModified)) {
            assertEquals(Status.OK, routes(Request(Method.GET, "/")).status)
            assertEquals(stockList, load())
        }
    }

    @Test
    fun `does update when lastModified was yesterday`() {
        val nextDayFromLastModified = Instant.parse("2022-02-10T00:00:00Z")
        with(Fixture(standardStockList, now = nextDayFromLastModified)) {
            assertEquals(Status.OK, routes(Request(Method.GET, "/")).status)
            assertNotEquals(stockList, load())
        }
    }

}
