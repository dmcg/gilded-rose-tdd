package com.gildedrose.persistence

import com.gildedrose.foundation.AnalyticsEvent

sealed interface StockListLoadingError : AnalyticsEvent {
    data class BlankID(val line: String) : StockListLoadingError
    data class BlankName(val line: String) : StockListLoadingError
    data class IOError(val message: String) : StockListLoadingError
}
