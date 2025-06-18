package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.http.ResponseErrors.attachedError
import com.gildedrose.testing.Given
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
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
import java.time.Instant


class AddItemHttpTests : AddItemAcceptanceContract(
    doAdd = ::addItemWithHttp
) {
    private val fixture = aSampleFixture(
        stockListLastModified = Instant.parse("2022-02-09T12:00:00Z"),
        now = Instant.parse("2022-02-09T23:59:59Z")
    )

    @Test
    fun validations() {
        val goodPost = postFormToAddItemsRoute(withHTMX = true)
            .form("new-itemId", "new-id")
            .form("new-itemName", "new name")
            .form("new-itemSellBy", "2023-07-23")
            .form("new-itemQuality", "99")
        Given(fixture)
            .Then {
                assertThat(
                    routes(goodPost.formWithout("new-itemId")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemId", "")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.formWithout("new-itemName")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemName", "")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemSellBy", "2023-00-99")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.formWithout("new-itemQuality")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemQuality", "")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemQuality", "-1")),
                    hasStatus(Status.BAD_REQUEST)
                )
                assertThat(
                    routes(goodPost.replacingForm("new-itemQuality", "1.0")),
                    hasStatus(Status.BAD_REQUEST)
                )
            }
    }

    @Test
    fun `reports all form errors`() {
        val postWithTwoMissingFields = postFormToAddItemsRoute()
            .form("new-itemName", "new name")
            .form("new-itemSellBy", "2023-07-23")
            .form("new-itemQuality", "1.0")
        Given(fixture)
            .Then {
                assertThat(
                    app.addHandler(postWithTwoMissingFields),
                    hasStatus(Status.BAD_REQUEST) and
                        hasAttachedError(
                            NewItemFailedEvent("[formData 'new-itemId' is required, formData 'new-itemQuality' must be integer]")
                        )
                )
            }
    }
}

private fun addItemWithHttp(app: App<*>, newItem: Item) {
    val response = app.routes(
        postFormToAddItemsRoute().withFormFor(newItem)
    )
    assertThat(
        response,
        hasStatus(Status.OK) and hasJustATableElementBody()
    )
}

class AddItemHttpNoHtmxTests : AddItemAcceptanceContract(
    doAdd = ::addItemWithHttpNoHtmx
)

private fun addItemWithHttpNoHtmx(app: App<*>, newItem: Item) {
    val response = app.routes(
        postFormToAddItemsRoute(withHTMX = false)
            .withFormFor(newItem)
    )
    assertThat(
        response,
        hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
    )
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

private fun Request.withFormFor(newItem: Item): Request {
    return form("new-itemId", newItem.id.toString())
        .form("new-itemName", newItem.name.toString())
        .form("new-itemSellBy", newItem.sellByDate?.toString() ?: "")
        .form("new-itemQuality", newItem.quality.toString())
}

private fun postFormToAddItemsRoute(withHTMX: Boolean = true) =
    Request(Method.POST, "/add-item").header("Content-Type", "application/x-www-form-urlencoded").run {
        if (withHTMX) header("HX-Request", "true") else this
    }

fun hasJustATableElementBody() = hasBody(Regex("""\A\s*<table>.*</table>\s*\z""", RegexOption.DOT_MATCHES_ALL))
