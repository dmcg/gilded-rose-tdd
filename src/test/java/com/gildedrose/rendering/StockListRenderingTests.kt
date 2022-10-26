package com.gildedrose.rendering

import com.gildedrose.*
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
class StockListRenderingTests {

    val someTime = Instant.now()

    @Test
    fun `list stock`(approver: Approver) {
        val stockList = StockList(
            lastModified = someTime,
            items = listOf(
                testItem("banana", oct29.minusDays(1), 42),
                testItem("kumquat", oct29.plusDays(1), 101),
                testItem("undated", null, 50)
            )
        )
        val result = render(
            stockListResult = Success(stockList),
            now = Instant.parse("2021-10-29T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = false
        )
        approver.assertApproved(result)
    }

    @Test
    fun `list stock with pricing enabled`(approver: Approver) {
        val stockList = StockList(
            lastModified = someTime,
            items = listOf(
                testItem("banana", oct29.minusDays(1), 42).copy(price = Success(Price(100))),
                testItem("kumquat", oct29.plusDays(1), 101).copy(price = Failure(RuntimeException("simulated failure"))),
                testItem("undated", null, 50).copy(price = Success(null))
            )
        )
        val result = render(
            stockListResult = Success(stockList),
            now = Instant.parse("2021-10-29T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = true
        )
        approver.assertApproved(result)
    }

    @Test
    fun `reports errors`(approver: Approver) {
        val result = render(
            stockListResult = Failure(StockListLoadingError.BlankName("line")),
            now = Instant.parse("2021-10-29T12:00:00Z"),
            zoneId = londonZoneId,
            isPricingEnabled = false
        )
        approver.assertApproved(result)
    }
}
