package com.gildedrose.http

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.time.Duration

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
        expectThat(events)
            .hasSize(1)
            .withFirst {
                isA<HttpEvent>().and {
                    get(HttpEvent::uri).isEqualTo("/")
                    get(HttpEvent::method).isEqualTo(GET.toString())
                    get(HttpEvent::status).isEqualTo(OK.code)
                }
            }
    }

    @Test
    fun `reports slow transactions to analytics with an additional event`() {
        filter.then {
            Thread.sleep(25)
            Response(OK)
        }.invoke(Request(GET, "/"))
        expectThat(events)
            .hasSize(2)
            .withFirst {
                isA<HttpEvent>().and {
                    get(HttpEvent::uri).isEqualTo("/")
                    get(HttpEvent::method).isEqualTo(GET.toString())
                    get(HttpEvent::status).isEqualTo(OK.code)
                }
            }
            .withElementAt(1) {
                isA<SlowHttpEvent>().and {
                    get(SlowHttpEvent::uri).isEqualTo("/")
                    get(SlowHttpEvent::method).isEqualTo(GET.toString())
                    get(SlowHttpEvent::status).isEqualTo(OK.code)
                }
            }
    }
}
