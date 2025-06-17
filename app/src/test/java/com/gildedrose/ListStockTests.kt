package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.rendering.render
import com.gildedrose.testing.fake
import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasStatus
import org.http4k.strikt.bodyString
import org.http4k.strikt.status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.Instant.parse as t

class ListStockTests {

    @Test
    fun `list stock`() {
        val fixture = aSampleFixture(stockListLastModified = t("2022-02-09T12:00:00Z"))
        val app = fixture.createApp(now = t("2022-02-09T23:59:59Z"))
        val expectedPricedStocklist = Success(fixture.originalPricedStockList)
        assertEquals(
            expectedPricedStocklist,
            app.loadStockList()
        )
        val response = app.routes(Request(GET, "/"))
        expectThat(response) {
            status.isEqualTo(OK)
            bodyString.isEqualTo(
                app.expectedRenderingFor(expectedPricedStocklist)
            )
        }
    }

    @Test
    fun `reports errors`() {
        val expectedError = StockListLoadingError.BlankName("B1\t\t2022-02-08\t42")

        val itemsThatFails = object : Items<NoTX> by fake() {
            override fun <R> inTransaction(block: context(NoTX) () -> R) = block(NoTX)

            context(NoTX) override fun load(): Result<StockList, StockListLoadingError> {
                return Failure(expectedError)
            }
        }
        val events: MutableList<Any> = mutableListOf()

        val app = App(
            items = itemsThatFails,
            pricing = { throw NotImplementedError() },
            clock = Instant::now,
            analytics = events::add
        )

        assertEquals(
            Failure(expectedError),
            app.loadStockList()
        )
        assertThat(app.routes(Request(GET, "/")), hasStatus(INTERNAL_SERVER_ERROR))
        assertEquals(
            expectedError,
            events.first()
        )
    }
}

fun App<*>.expectedRenderingFor(
    pricedStocklist: Success<PricedStockList>
) = render(pricedStocklist, clock(), londonZoneId, justTable = false).bodyString()


