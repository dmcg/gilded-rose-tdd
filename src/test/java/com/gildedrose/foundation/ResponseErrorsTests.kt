package com.gildedrose.foundation

import com.gildedrose.http.ResponseErrorsImpl
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResponseErrorsTests {

    private val lookup = mutableMapOf<String, AnalyticsEvent>()
    private val responseErrors = ResponseErrorsImpl("OUR_HEADER", lookup)
    private val events = mutableListOf<AnalyticsEvent>()
    private val filter = responseErrors.reportTo(events::add)

    @Test
    fun `does nothing if no error populated`() {
        assertEquals(
            Response(Status.OK),
            filter.then { Response(Status.OK) }.invoke(aRequest)
        )
        assertEquals(0, events.size)
    }

    @Test
    fun `reports error if populated`() {
        assertEquals(
            Response(Status.OK),
            with(responseErrors) {
                filter.then { Response(Status.OK).withError(MyTestEvent) }.invoke(aRequest)
            }
        )
        assertEquals(listOf(MyTestEvent), events)
    }

    @Test
    fun `clears lookup and removes header`() {
        assertEquals(
            0,
            with(responseErrors) {
                filter.then { Response(Status.OK).withError(MyTestEvent) }.invoke(aRequest).headers.size
            }
        )
        assertEquals(0, lookup.size)
    }

    @Test
    fun `clears lookup even on exception in analytics`() {
        val filter = responseErrors.reportTo { error("deliberate") }

        assertThrows<IllegalStateException> {
            with(responseErrors) {
                filter.then { Response(Status.OK).withError(MyTestEvent) }.invoke(aRequest).headers.size
            }

        }
        assertEquals(0, lookup.size)
    }
}

private val aRequest = Request(GET, "/")

private object MyTestEvent : AnalyticsEvent

