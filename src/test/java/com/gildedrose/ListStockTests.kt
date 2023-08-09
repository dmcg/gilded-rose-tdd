package com.gildedrose

import arrow.core.raise.Raise
import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.*
import com.gildedrose.testing.IOResolver
import com.gildedrose.testing.fake
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
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

context(IO)
@ExtendWith(IOResolver::class)
@ExtendWith(ApprovalTest::class)
class ListStockTests {

    private val lastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    @Test
    fun `list stock`(approver: Approver) {
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
            items = fixture.unpricedItems,
            pricing = fixture::pricing,
            clock = { sameDayAsLastModified }
        )
        assertEquals(
            Success(pricedStockList),
            app.loadStockList()
        )
        approver.assertApproved(app.routes(Request(GET, "/")), OK)
    }

    @Test
    fun `reports errors`() {
        val expectedError = StockListLoadingError.BlankName("B1\t\t2022-02-08\t42")

        val itemsThatFails = object : Items<NoTX> by fake() {
            override fun <R> inTransaction(block: context(NoTX) () -> R) = block(NoTX)

            context(IO, NoTX, Raise<StockListLoadingError>)
            override fun load(): StockList {
                raise(expectedError)
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
