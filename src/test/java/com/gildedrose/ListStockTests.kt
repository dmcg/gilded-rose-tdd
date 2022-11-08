package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
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
import java.time.LocalDate

@ExtendWith(ApprovalTest::class)
class ListStockTests {

    companion object {
        private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")
        private val nextDayFromLastModified = Instant.parse("2022-02-10T00:00:00Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", LocalDate.parse("2022-02-08"), 42),
                testItem("kumquat", LocalDate.parse("2022-02-10"), 101),
                testItem("undated", null, 50)
            )
        )
        private val baseApp = App()
    }

    @Test
    fun `list stock`(approver: Approver) {
        with(
            baseApp.fixture(
                now = sameDayAsLastModified,
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
            baseApp.fixture(
                now = sameDayAsLastModified,
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
        with(
            baseApp.fixture(
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
        with(
            baseApp.fixture(
                now = nextDayFromLastModified,
                initialStockList = stockList
            )
        ) {
            val expectedUpdatedStockList = StockList(
                lastModified = nextDayFromLastModified,
                items = listOf(
                    stockList.items[0].copy(quality = Quality(40)!!),
                    stockList.items[1].copy(quality = Quality(100)!!),
                    stockList.items[2]
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
            baseApp.fixture(
                now = sameDayAsLastModified,
                initialStockList = stockList
            )
        ) {
            stockFile.writeText(stockFile.readText().replace("banana", ""))
            val expectedFailure = StockListLoadingError.BlankName("B1\t\t2022-02-08\t42")
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
