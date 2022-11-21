package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.pricing.fakeValueElfRoutes
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

@ExtendWith(ApprovalTest::class)
class ListStockTests {

    companion object {
        private val lastModified = t("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                testItem("banana", localDate("2022-02-08"), 42),
                testItem("kumquat", localDate("2022-02-10"), 101),
                testItem("undated", null, 50)
            )
        )
        private val valueElfPricing = { id: ID<Item>, quality: Quality ->
            when (id) {
                stockList[0].id -> Price(666)
                stockList[1].id -> null
                stockList[2].id -> Price(999)
                else -> error("Unexpected item for pricing")
            }
        }
        private val expectedPricedStockList = stockList.withItems(
            stockList[0].withPrice(Price(666)),
            stockList[1].withPrice(null),
            stockList[2].withPrice(Price(999))
        )

        @BeforeAll
        @JvmStatic
        fun startServer() {
            server.start()
        }

        private val baseApp = App(valueElfUri = URI.create("http://localhost:8888/prices"))
        private val server = serverFor(port = 8888, fakeValueElfRoutes(valueElfPricing))

        @AfterAll
        @JvmStatic
        fun stopServer() {
            server.stop()
        }
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
                Success(expectedPricedStockList),
                app.loadStockList()
            )
            approver.assertApproved(routes(Request(GET, "/")), OK)
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
