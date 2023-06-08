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
                item("banana", oct29.minusDays(1), 42).withPriceResult(Price(100)),
                item("kumquat", oct29.plusDays(1), 101).withPriceResult(Failure(RuntimeException("simulated failure"))),
                item("undated", null, 50).withPriceResult(null)
            )
        )
        approver.assertApproved(
            render(
                stockListResult = Success(stockList),
                now = Instant.parse("2021-10-29T12:00:00Z"),
                zoneId = londonZoneId,
                features = Features(isDeletingEnabled = false)
            )
        )
    }

    @Test
    fun `list stock with deleting enabled`(approver: Approver) {
        val stockList = StockList(
            lastModified = someTime,
            items = listOf(
                item("banana", oct29.minusDays(1), 42).withPriceResult(Price(100)),
                item("kumquat", oct29.plusDays(1), 101).withPriceResult(Failure(RuntimeException("simulated failure"))),
                item("undated", null, 50).withPriceResult(null)
            )
        )
        approver.assertApproved(
            render(
                stockListResult = Success(stockList),
                now = Instant.parse("2021-10-29T12:00:00Z"),
                zoneId = londonZoneId,
                features = Features(isDeletingEnabled = true)
            )
        )
    }

    @Test
    fun `reports errors`(approver: Approver) {
        approver.assertApproved(
            render(
                stockListResult = Failure(StockListLoadingError.BlankName("line")),
                now = Instant.parse("2021-10-29T12:00:00Z"),
                zoneId = londonZoneId,
                features = Features(isDeletingEnabled = false)
            )
        )
    }
}
