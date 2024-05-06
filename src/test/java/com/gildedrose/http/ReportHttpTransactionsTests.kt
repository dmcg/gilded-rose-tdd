package com.gildedrose.http

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportHttpTransactionsTests {

    private val events: MutableList<Any> = mutableListOf()
    private val filter = reportHttpTransactions(Duration.ofMillis(20)) { event ->
        events.add(event)
    }

    @Test
    fun `reports transactions to analytics`() {
        filter.then {
            Response(OK)
        }.invoke(Request(GET, "/"))

        val event = events.single()
        assertTrue(event is HttpEvent)
        assertEquals("/", event.uri)
        assertEquals(GET.toString(), event.method)
        assertEquals(OK.code, event.status)
    }

    @Test
    fun `reports slow transactions to analytics with an additional event`() {
        filter.then {
            Thread.sleep(25)
            Response(OK)
        }.invoke(Request(GET, "/"))

        val event0 = events.first()
        assertTrue(event0 is HttpEvent)
        assertEquals("/", event0.uri)
        assertEquals(GET.toString(), event0.method)
        assertEquals(OK.code, event0.status)

        val event1 = events[1]
        assertTrue(event1 is SlowHttpEvent)
        assertEquals("/", event1.uri)
        assertEquals(GET.toString(), event1.method)
        assertEquals(OK.code, event1.status)
    }
}
