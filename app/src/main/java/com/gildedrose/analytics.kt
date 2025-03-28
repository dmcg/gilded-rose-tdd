package com.gildedrose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.AnalyticsEvent
import org.http4k.filter.ZipkinTracesStorage
import java.time.Instant

fun loggingAnalytics(
    logger: (String) -> Unit,
    objectMapper: ObjectMapper = loggingObjectMapper(),
    clock: () -> Instant = Instant::now
) : Analytics = { event: AnalyticsEvent ->
    val traces = ZipkinTracesStorage.THREAD_LOCAL.forCurrentThread()
    val envelope = Envelope(
        clock(),
        traces.traceId.value,
        traces.spanId.value,
        traces.parentSpanId?.value,
        event
    )
    logger(objectMapper.writeValueAsString(envelope))
}

@Suppress("unused")
class Envelope(
    val timestamp: Instant,
    val traceId: String,
    val spanId: String,
    val parentSpanId: String?,
    val event: AnalyticsEvent
)

internal fun loggingObjectMapper(): ObjectMapper = jacksonObjectMapper().apply {
    registerModule(ParameterNamesModule())
    registerModule(Jdk8Module())
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
}

