package com.gildedrose.persistence

import com.gildedrose.domain.ItemCreationError

sealed interface StockListLoadingError {
    data class CouldntParseLastModified(val message: String) : StockListLoadingError
    data class CouldntCreateItem(val reason: ItemCreationError) : StockListLoadingError
    data class NotEnoughFields(val line: String) : StockListLoadingError
    data class CouldntParseSellBy(val message: String) : StockListLoadingError
    data class CouldntParseQuality(val line: String) : StockListLoadingError
    data class IO(val message: String) : StockListLoadingError
}
