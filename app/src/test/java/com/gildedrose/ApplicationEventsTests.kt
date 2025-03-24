package com.gildedrose

import com.gildedrose.foundation.UncaughtExceptionEvent
import com.gildedrose.http.HttpEvent
import com.gildedrose.testing.InMemoryItems
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

class ApplicationEventsTests {

    private val events = mutableListOf<Any>()
    private val app = App(
        items = InMemoryItems(),
        pricing = { null },
        analytics = events::add
    )

    @Test
    fun `uncaught exceptions raise an event`() {
        assertEquals(0, events.size)

        val response = app.routes(Request(GET, "/error"))
        assertThat(
            response,
            hasStatus(INTERNAL_SERVER_ERROR) and
                hasBody("Something went wrong, sorry.")
        )
        assertEquals(UncaughtExceptionEvent::class, events[0]::class)
        assertEquals(HttpEvent::class, events[1]::class)
    }

    @Test
    fun `every request raises an event`() {
        assertEquals(0, events.size)

        val response = app.routes(Request(GET, "/"))
        assertEquals(OK, response.status)
        assertEquals(HttpEvent::class, events.single()::class)
    }
}
