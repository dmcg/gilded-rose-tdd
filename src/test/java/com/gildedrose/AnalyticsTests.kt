package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AnalyticsTests {

    @Test
    fun `outputs json of the events`() {
        val logged = mutableListOf<String>()
        val now = Instant.parse("2022-04-14T15:13:49.688906Z")
        val analytics = LoggingAnalytics(
            logger = logged::add,
            clock = { now }
        )

        assertEquals(0, logged.size)
        analytics(TestEvent("banana"))
        assertEquals(
            listOf("""{"timestamp":"2022-04-14T15:13:49.688906Z","event":{"value":"banana","eventName":"TestEvent"}}"""),
            logged
        )
    }
}



data class TestEvent(val value: String) : AnalyticsEvent

