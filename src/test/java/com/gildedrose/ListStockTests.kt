package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(ApprovalTest::class)
class ListStockTests {

    private val stockList = StockList(
        lastModified = Instant.parse("2022-02-09T12:00:00Z"),
        items = listOf(
            testItem("banana", oct29.minusDays(1), 42),
            testItem("kumquat", oct29.plusDays(1), 101),
            testItem("undated", null, 50)
        )
    )

    @Test
    fun `list stock`(approver: Approver) {
        with(
            App().fixture(
                now = Instant.parse("2021-10-29T12:00:00Z"),
                initialStockList = stockList
            )
        ) {
            assertEquals(
                Success(stockList.withNullPrices()),
                app.loadStockList()
            )
            approver.assertApproved(routes(Request(GET, "/")), OK)
        }
    }

    @Test
    fun `list stock sees file updates`() {
        with(
            App().fixture(
                now = Instant.parse("2021-10-29T12:00:00Z"),
                initialStockList = stockList
            )
        ) {
            val savedStockList = StockList(Instant.now(), emptyList())
            save(savedStockList)
            assertEquals(
                Success(savedStockList),
                app.loadStockList()
            )
        }
    }

    @Test
    fun `doesn't update when lastModified is today`() {
        val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
        with(
            App().fixture(
                now = sameDayAsLastModified,
                initialStockList = stockList
            )
        ) {
            assertEquals(
                Success(stockList.withNullPrices()),
                app.loadStockList()
            )
            assertEquals(stockList, load())
        }
    }

    @Test
    fun `does update when lastModified was yesterday`() {
        val nextDayFromLastModified = Instant.parse("2022-02-10T00:00:00Z")
        with(
            App().fixture(
                now = nextDayFromLastModified,
                initialStockList = stockList
            )
        ) {
            val expectedUpdatedStockList = StockList(
                lastModified = nextDayFromLastModified,
                items = listOf(
                    testItem("banana", oct29.minusDays(1), 40),
                    testItem("kumquat", oct29.plusDays(1), 99),
                    testItem("undated", null, 50)
                )
            )
            assertEquals(
                Success(expectedUpdatedStockList.withNullPrices()),
                app.loadStockList()
            )
            assertEquals(expectedUpdatedStockList, load())
        }
    }

    @Test
    fun `reports errors`() {
        with(
            App().fixture(
                now = Instant.parse("2022-02-10T00:00:00Z"),
                initialStockList = stockList
            )
        ) {
            stockFile.writeText(stockFile.readText().replace("banana", ""))
            val expectedFailure = StockListLoadingError.BlankName("B1\t\t2021-10-28\t42")
            assertEquals(
                Failure(expectedFailure),
                app.loadStockList()
            )
            assertThat(routes(Request(GET, "/")), hasStatus(INTERNAL_SERVER_ERROR))
            assertEquals(
                expectedFailure,
                events.first()
            )
        }
    }
}

private fun StockList.withNullPrices() = this.copy(items = items.map { it.withNullPrice()})

private fun Item.withNullPrice() = this.copy(price = Success(null))
