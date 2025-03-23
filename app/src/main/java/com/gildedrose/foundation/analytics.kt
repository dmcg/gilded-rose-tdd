package com.gildedrose.foundation

@Suppress("unused")
interface AnalyticsEvent {
    val eventName: String get() = this::class.simpleName ?: "Event Name Unknown"
}

typealias Analytics = (AnalyticsEvent) -> Unit
