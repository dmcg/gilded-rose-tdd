package com.gildedrose

import com.gildedrose.config.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.IO
import com.gildedrose.http.ResponseErrors.attachedError
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.persistence.transactionally
import com.gildedrose.testing.IOResolver
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import dev.forkhandles.result4k.Success
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.body.toBody
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
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
        checkStocklistHas(
            sameDayAsLastModified,
            fixture.stockList[0],
            newItem
        )
    }

    @Test
    fun `add item via http`() {
        val response = app.routes(
            postFormToAddItemsRoute()
                .form("new-itemId", "new-id")
                .form("new-itemName", "new name")
                .form("new-itemSellBy", "2023-07-23")
                .form("new-itemQuality", "99")
        )
        assertThat(
            response,
            hasStatus(Status.OK) and hasJustATableElementBody()
        )
        checkStocklistHas(
            sameDayAsLastModified,
            fixture.stockList[0],
            item("new-id", "new name", localDate("2023-07-23"), 99)
        )
    }

    @Test
    fun `add item with no date via http`() {
        val response = app.routes(
            postFormToAddItemsRoute()
                .form("new-itemId", "new-id")
                .form("new-itemName", "new name")
                .form("new-itemQuality", "99")
        )
        assertThat(
            response,
            hasStatus(Status.OK) and hasJustATableElementBody()
        )
        checkStocklistHas(
            sameDayAsLastModified,
            fixture.stockList[0],
            item("new-id", "new name", null, 99)
        )
    }

    @Test
    fun `add item with blank date via http`() {
        val response = app.routes(
            postFormToAddItemsRoute()
                .form("new-itemId", "new-id")
                .form("new-itemName", "new name")
                .form("new-itemSellBy", "")
                .form("new-itemQuality", "99")
        )
        assertThat(
            response,
            hasStatus(Status.OK) and hasJustATableElementBody()
        )
        checkStocklistHas(
            sameDayAsLastModified,
            fixture.stockList[0],
            item("new-id", "new name", null, 99)
        )
    }

    @Test
    fun `add item via http with no htmx`() {
        val response = app.routes(
            postFormToAddItemsRoute(withHTMX = false)
                .form("new-itemId", "new-id")
                .form("new-itemName", "new name")
                .form("new-itemSellBy", "2023-07-23")
                .form("new-itemQuality", "99")
        )
        assertThat(
            response,
            hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
        )
        checkStocklistHas(
            sameDayAsLastModified,
            fixture.stockList[0],
            item("new-id", "new name", localDate("2023-07-23"), 99)
        )
    }

    @Test
    fun validations() {
        val goodPost = postFormToAddItemsRoute(withHTMX = true)
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
            app.routes(goodPost.replacingForm("new-itemSellBy", "2023-00-99")),
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
        assertThat(
            app.routes(goodPost.replacingForm("new-itemQuality", "-1")),
            hasStatus(Status.BAD_REQUEST)
        )
        assertThat(
            app.routes(goodPost.replacingForm("new-itemQuality", "1.0")),
            hasStatus(Status.BAD_REQUEST)
        )
    }

    @Test
    fun `reports all form errors`() {
        val postWithTwoMissingFields = postFormToAddItemsRoute()
            .form("new-itemName", "new name")
            .form("new-itemSellBy", "2023-07-23")
            .form("new-itemQuality", "1.0")
        assertThat(
            app.addHandler(postWithTwoMissingFields),
            hasStatus(Status.BAD_REQUEST) and
                hasAttachedError(
                    NewItemFailedEvent("[formData 'new-itemId' is required, formData 'new-itemQuality' must be integer]")
                )
        )
    }

    private fun checkStocklistHas(lastModified: Instant, vararg items: Item) {
        assertEquals(Success(StockList(lastModified, items.toList())),
            fixture.unpricedItems.transactionally { load() }
        )
    }
}

private fun Request.formWithout(parameterName: String): Request =
    body(form().filter { it.first != parameterName }.toBody())

private fun Request.replacingForm(parameterName: String, parameterValue: String) =
    formWithout(parameterName).form(parameterName, parameterValue)

private fun hasAttachedError(event: AnalyticsEvent): Matcher<Response> =
    has(
        "attached error",
        { response -> response.attachedError },
        equalTo(event)
    )

private fun postFormToAddItemsRoute(withHTMX: Boolean = true) =
    Request(Method.POST, "/add-item").header("Content-Type", "application/x-www-form-urlencoded").run {
        if (withHTMX) header("HX-Request", "true") else this
    }

private fun hasJustATableElementBody() = hasBody(Regex("""\A\s*<table>.*</table>\s*\z""", RegexOption.DOT_MATCHES_ALL))
