package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFrom
import java.time.Instant

class PricedStockListLoader(
    val loading: (Instant) -> Result<StockList, StockListLoadingError>,
    val pricing: (Item) -> Price?
) {
    fun load(now: Instant): Result<StockList, StockListLoadingError> =
        loading(now).map { it.pricedBy(pricing) }
}

private fun StockList.pricedBy(pricing: (Item) -> Price?): StockList =
    this.copy(items = items.map { it.pricedBy(pricing) })

private fun Item.pricedBy(pricing: (Item) -> Price?): Item =
    this.copy(price = resultFrom { pricing(this) })
