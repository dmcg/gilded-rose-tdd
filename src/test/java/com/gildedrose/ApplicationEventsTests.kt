package com.gildedrose

import HttpEvent
import UncaughtExceptionEvent
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplicationEventsTests {

    private val fixture = Fixture(
        StockList(
            lastModified = Instant.parse("2022-02-09T12:00:00Z"),
            items = emptyList()
        ), now = Instant.now()
    )

    @Test
    fun `uncaught exceptions raise an event`() {
        with(fixture) {
            assertEquals(0, events.size)

            val response = routes(Request(GET, "/error"))
            assertThat(response,
                hasStatus(INTERNAL_SERVER_ERROR) and
                    hasBody("Something went wrong, sorry.")
            )
            assertEquals(UncaughtExceptionEvent::class, events[0]::class)
            assertEquals(HttpEvent::class, events[1]::class)
        }
    }

    @Test
    fun `every request raises an event`() {
        with(fixture) {
            assertEquals(0, events.size)

            val response = routes(Request(GET, "/"))
            assertEquals(OK, response.status)
            assertEquals(HttpEvent::class, events.only()::class)
        }
    }
}

private fun <E> Collection<E>.only(): E =
    when {
        this.size != 1 -> error("Expected one item, got $this")
        else -> this.first()
    }
