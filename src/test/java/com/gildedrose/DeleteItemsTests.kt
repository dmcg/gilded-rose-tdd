package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.*
import com.gildedrose.testing.IOResolver
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.result4k.Success
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

context(IO)
@ExtendWith(IOResolver::class)
class DeleteItemsTests {

    private val lastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    val pricedStockList = PricedStockList(
        lastModified,
        listOf(
            item("banana", localDate("2022-02-08"), 42).withPriceResult(Price(666)),
            item("kumquat", localDate("2022-02-10"), 101).withPriceResult(null),
            item("undated", null, 50).withPriceResult(Price(999))
        )
    )
    val fixture = Fixture(pricedStockList, InMemoryItems()).apply { init() }
    val app = App(
        items = fixture.unpricedItems,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified }
    )

    @Test
    fun `delete items`() {
        app.deleteItemsWithIds(setOf(
            pricedStockList[0].id,
            pricedStockList[2].id))
        assertEquals(
            Success(StockList(sameDayAsLastModified, listOf(pricedStockList[1].withNoPrice()))),
            fixture.unpricedItems.transactionally { load() }
        )
    }

    @Test
    fun `delete no items doesnt save stocklist`() {
        app.deleteItemsWithIds(emptySet())
        assertEquals(
            Success(fixture.stockList),
            fixture.unpricedItems.transactionally { load() }
        )
    }

    @Test
    fun `delete non-existent item doesnt save stocklist`() {
        app.deleteItemsWithIds(setOf(ID("no-such")!!))
        assertEquals(
            Success(fixture.stockList),
            fixture.unpricedItems.transactionally { load() }
        )
    }

    @Test
    fun `delete items via http`() {
        val response = app.routes(
            Request(Method.POST, "/delete-items")
                .form(pricedStockList[0].id.toString(), "on")
                .form(pricedStockList[2].id.toString(), "on")
        )
        assertThat(
            response,
            hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
        )
        assertEquals(
            Success(StockList(sameDayAsLastModified, listOf(pricedStockList[1].withNoPrice()))),
            fixture.unpricedItems.transactionally { load() }
        )
    }
}
