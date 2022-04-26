package com.gildedrose

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
class ListStockTests {

    private val stockList = StockList(
        lastModified = Instant.parse("2022-02-09T12:00:00Z"),
        items = listOf(
            Item("banana", oct29.minusDays(1), 42),
            Item("kumquat", oct29.plusDays(1), 101),
            Item("undated", null, 50)
        )
    )

    @Test
    fun `list stock`(approver: Approver) {
        with(
            Fixture(stockList, now = Instant.parse("2021-10-29T12:00:00Z"))
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }
    }

    @Test
    fun `list stock sees file updates`(approver: Approver) {
        with(
            Fixture(stockList, now = Instant.parse("2021-10-29T12:00:00Z"))
        ) {
            assertEquals(OK, routes(Request(GET, "/")).status)

            save(StockList(Instant.now(), emptyList()))
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }
    }

    @Test
    fun `doesn't update when lastModified is today`(approver: Approver) {
        val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
        with(
            Fixture(stockList, now = sameDayAsLastModified)
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
            assertEquals(stockList, load())
        }
    }

    @Test
    fun `does update when lastModified was yesterday`(approver: Approver) {
        val nextDayFromLastModified = Instant.parse("2022-02-10T00:00:00Z")
        with(
            Fixture(stockList, now = nextDayFromLastModified)
        ) {
            approver.assertApproved(routes(Request(GET, "/")), OK)
            Assertions.assertNotEquals(stockList, load())
        }
    }
}
