package com.gildedrose

import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.http.ResponseErrors.attachedError
import com.gildedrose.testing.Given
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.body.toBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test
import java.time.Instant


class AddItemHttpTests : AddItemAcceptanceContract(
    alison = HttpActor()
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

class AddItemHttpNoHtmxTests : AddItemAcceptanceContract(
    alison = HttpNoHtmxActor()
)

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
