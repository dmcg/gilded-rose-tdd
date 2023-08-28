package com.gildedrose

import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.NoTX
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.rendering.render
import com.gildedrose.testing.IOResolver
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
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

context(IO)
@ExtendWith(IOResolver::class)
class ListStockTests {

    private val lastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    @Test
    fun `list stock`() {
        val pricedStockList = PricedStockList(
            lastModified,
            listOf(
                item("banana", localDate("2022-02-08"), 42).withPriceResult(Price(666)),
                item("kumquat", localDate("2022-02-10"), 101).withPriceResult(null),
                item("undated", null, 50).withPriceResult(Price(999))
            )
        )
        val fixture = Fixture(pricedStockList).apply { init() }
        val app = App(
            items = fixture.items,
            pricing = fixture::pricing,
            clock = { sameDayAsLastModified }
        )
        val expectedPricedStocklist = Success(pricedStockList)
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

            context(IO, NoTX) override fun load(): Result<StockList, StockListLoadingError> {
                return Failure(expectedError)
            }
        }
        val events: MutableList<Any> = mutableListOf()

        val app = App(
            items = itemsThatFails,
            pricing = { throw NotImplementedError() },
            clock = { sameDayAsLastModified },
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

fun App.expectedRenderingFor(
    pricedStocklist: Success<PricedStockList>
) = render(pricedStocklist, clock(), londonZoneId, features, justTable = false).bodyString()


