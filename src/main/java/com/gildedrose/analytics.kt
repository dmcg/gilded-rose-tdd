package com.gildedrose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.time.Instant

@Suppress("unused")
interface AnalyticsEvent {
    val eventName: String get() = this::class.simpleName ?: "Event Name Unknown"
}

typealias Analytics = (AnalyticsEvent) -> Unit

class LoggingAnalytics(
    private val logger: (String) -> Unit,
    private val objectMapper: ObjectMapper = loggingObjectMapper(),
    private val clock: () -> Instant = Instant::now
) : Analytics {

    override fun invoke(event: AnalyticsEvent) {
        logger(objectMapper.writeValueAsString(Envelope(clock(), event)))
    }

    @Suppress("unused")
    class Envelope(
        val timestamp: Instant,
        val event: AnalyticsEvent
    )
}

private fun loggingObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerModule(ParameterNamesModule())
    registerModule(Jdk8Module())
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
}

