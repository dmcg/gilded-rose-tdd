package com.gildedrose

import com.gildedrose.foundation.AnalyticsEvent
import org.http4k.filter.TraceId
import org.http4k.filter.ZipkinTraces
import org.http4k.filter.ZipkinTracesStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AnalyticsTests {

    @Test
    fun `outputs json of the events`() {
        val logged = mutableListOf<String>()
        val now = Instant.parse("2022-04-14T15:13:49.688906Z")
        val analytics = loggingAnalytics(
            logger = logged::add,
            clock = { now }
        )

        assertEquals(0, logged.size)

        withTraces(ZipkinTraces(TraceId("trace"), TraceId("span"), TraceId("parent"))) {
            analytics(TestEvent("banana"))
            assertEquals(
                listOf("""{"timestamp":"2022-04-14T15:13:49.688906Z","traceId":"trace","spanId":"span","parentSpanId":"parent","event":{"value":"banana","eventName":"TestEvent"}}"""),
                logged
            )
        }
    }
}

data class TestEvent(val value: String) : AnalyticsEvent

private fun withTraces(traces: ZipkinTraces, f: () -> Unit) {
    val oldTraces = ZipkinTracesStorage.THREAD_LOCAL.forCurrentThread()
    ZipkinTracesStorage.THREAD_LOCAL.setForCurrentThread(traces)
    try {
        f()
    } finally {
        ZipkinTracesStorage.THREAD_LOCAL.setForCurrentThread(oldTraces)
    }
}
