package com.gildedrose.rendering

import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.item
import com.gildedrose.londonZoneId
import com.gildedrose.oct29
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.withPriceResult
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

    private val pricedStockList: PricedStockList = PricedStockList(
        lastModified = someTime,
        items = listOf(
            item("banana", oct29.minusDays(1), 42).withPriceResult(Price(100)),
            item(
                "kumquat",
                oct29.plusDays(1),
                101
            ).withPriceResult(Failure(RuntimeException("simulated failure"))),
            item("undated", null, 50).withPriceResult(null)
        )
    )

    @Test
    fun `list stock`(approver: Approver) {
        approver.assertApproved(
            render(
                stockListResult = Success(pricedStockList),
                now = Instant.parse("2021-10-29T12:00:00Z"),
                zoneId = londonZoneId,
                justTable = false
            )
        )
    }

    @Test
    fun `list just stock table`(approver: Approver) {
        approver.assertApproved(
            render(
                stockListResult = Success(pricedStockList),
                now = Instant.parse("2021-10-29T12:00:00Z"),
                zoneId = londonZoneId,
                justTable = true
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
                justTable = true
            )
        )
    }
}
