package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.rendering.render
import com.gildedrose.testing.Given
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
import kotlin.test.assertTrue
import java.time.Instant.parse as t


class ListStockTests {

    @Test
    fun `list stock directly`() {
        Given(aSampleFixture(
            stockListLastModified = t("2022-02-09T12:00:00Z"),
            now = t("2022-02-09T23:59:59Z"))
        ).When {
            app.loadStockList()
        }.Then { result ->
            expectThat(originalStockList).isEqualTo(currentStockList())
        }
    }

    @Test
    fun `list stock http`() {
        Given(aSampleFixture(
            stockListLastModified = t("2022-02-09T12:00:00Z"),
            now = t("2022-02-09T23:59:59Z"))
        ).When {
            routes(Request(GET, "/"))
        }.Then {
            expectThat(it) {
                status.isEqualTo(OK)
                bodyString.isEqualTo(
                    app.expectedRenderingFor(Success(originalPricedStockList))
                )
            }
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
        assertTrue(
            events.isEmpty()
        )
        assertThat(
            app.routes(Request(GET, "/")),
            hasStatus(INTERNAL_SERVER_ERROR)
        )
        assertEquals(
            expectedError,
            events.first()
        )
        println(events)
    }
}

fun App<*>.expectedRenderingFor(
    pricedStocklist: Success<PricedStockList>
) = render(pricedStocklist, clock(), londonZoneId, justTable = false).bodyString()


