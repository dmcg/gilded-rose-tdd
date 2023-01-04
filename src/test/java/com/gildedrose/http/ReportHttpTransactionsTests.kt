package com.gildedrose.http

import com.gildedrose.testing.assertAll
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

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
        assertAll(
            events.single() as HttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(GET.toString(), method) },
            { assertEquals(OK.code, status) },
        )
    }

    @Test
    fun `reports slow transactions to analytics with an additional event`() {
        filter.then {
            Thread.sleep(25)
            Response(OK)
        }.invoke(Request(GET, "/"))
        assertAll(
            events.first() as HttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(GET.toString(), method) },
            { assertEquals(OK.code, status) },
        )
        assertAll(
            events[1] as SlowHttpEvent,
            { assertEquals("/", uri) },
            { assertEquals(GET.toString(), method) },
            { assertEquals(OK.code, status) },
        )
    }
}
