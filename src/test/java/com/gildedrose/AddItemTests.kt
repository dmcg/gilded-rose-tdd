package com.gildedrose

import com.gildedrose.config.Features
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
import org.http4k.core.body.toBody
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

context(IO)
@ExtendWith(IOResolver::class)
class AddItemTests {

    private val lastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    val pricedStockList = PricedStockList(
        lastModified,
        listOf(
            item("banana", localDate("2022-02-08"), 42).withPriceResult(Price(666)),
        )
    )
    val fixture = Fixture(pricedStockList, InMemoryItems()).apply { init() }
    val app = App(
        items = fixture.unpricedItems,
        pricing = fixture::pricing,
        clock = { sameDayAsLastModified },
        features = Features(newItemEnabled = true)
    )

    @Test
    fun `add item`() {
        val newItem = item("new-id", "new name", localDate("2023-07-23"), 99)
        app.addItem(newItem)
        assertEquals(
            Success(
                StockList(
                    sameDayAsLastModified,
                    listOf(
                        fixture.stockList[0],
                        newItem
                    )
                )
            ),
            fixture.unpricedItems.transactionally { load() }
        )
    }

    @Test
    fun `add item via http`() {
        val response = app.routes(
            Request(Method.POST, "/add-item")
                .form("new-itemId", "new-id")
                .form("new-itemName", "new name")
                .form("new-itemSellBy", "2023-07-23")
                .form("new-itemQuality", "99")
        )
        assertThat(
            response,
            hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
        )
        assertEquals(
            Success(
                StockList(
                    sameDayAsLastModified,
                    listOf(
                        fixture.stockList[0],
                        item("new-id", "new name", localDate("2023-07-23"), 99)
                    )
                )
            ),
            fixture.unpricedItems.transactionally { load() }
        )
        // TODO - check blank fields, invalid date, negative qualities
    }

    @Test
    fun `validations`() {
        val goodPost = Request(Method.POST, "/add-item")
            .form("new-itemId", "new-id")
            .form("new-itemName", "new name")
            .form("new-itemSellBy", "2023-07-23")
            .form("new-itemQuality", "99")
        assertThat(
            app.routes(goodPost.formWithout("new-itemId")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.replacingForm("new-itemId", "")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.formWithout("new-itemName")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.replacingForm("new-itemName", "")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.formWithout("new-itemSellBy")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.replacingForm("new-itemSellBy", "")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.formWithout("new-itemQuality")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.replacingForm("new-itemQuality", "")),
            hasStatus(Status.BAD_REQUEST)
        )
    }
}

private fun Request.formWithout(parameterName: String): Request =
    body(form().filter { it.first != parameterName }.toBody())

private fun Request.replacingForm(parameterName: String, parameterValue: String) =
    formWithout(parameterName).form(parameterName, parameterValue)
