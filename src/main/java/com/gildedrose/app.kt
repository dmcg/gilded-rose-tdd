package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.Stock
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFrom
import java.io.File
import java.time.Instant
import java.time.ZoneId

val stdOutAnalytics = loggingAnalytics(::println)
val londonZoneId = ZoneId.of("Europe/London")

data class App(
    val port: Int = 8080,
    val stockFile: File = File("stock.tsv"),
    val features: Features = Features(),
    val pricing: (Item) -> Price? = ::noOpPricing,
    val clock: () -> Instant = Instant::now,
    val analytics: Analytics = stdOutAnalytics
) {
    private val stock = Stock(stockFile, londonZoneId, Item::updatedBy)
    val routes = routesFor(
        clock = clock,
        analytics = analytics,
        features,
        ::loadStockList
    )
    private val server = serverFor(port = port, routes)

    fun loadStockList(now: Instant = clock()) =
        stock.stockList(now).map { it.pricedBy(pricing) }

    fun start() {
        server.start()
    }
}



private fun StockList.pricedBy(pricing: (Item) -> Price?): StockList =
    this.copy(items = items.map { it.pricedBy(pricing) })

private fun Item.pricedBy(pricing: (Item) -> Price?): Item =
    this.copy(price = resultFrom { pricing(this) })

@Suppress("UNUSED_PARAMETER")
fun noOpPricing(item: Item): Price? = null
