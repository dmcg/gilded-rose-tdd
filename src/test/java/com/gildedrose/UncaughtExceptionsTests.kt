package com.gildedrose

import UncaughtExceptionEvent
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class UncaughtExceptionsTests {

    private val stockList = StockList(
        lastModified = Instant.parse("2022-02-09T12:00:00Z"),
        items = emptyList()
    )

    @Test
    fun `uncaught exceptions`() {
        with(
            Fixture(stockList, now = Instant.now())
        ) {
            assertEquals(0, events.size)
            val response = routes(Request(GET, "/error"))
            assertEquals(
                Response(INTERNAL_SERVER_ERROR).body("Something went wrong, sorry."),
                response
            )
            assertEquals(UncaughtExceptionEvent::class, events.first()::class)
        }
    }
}
