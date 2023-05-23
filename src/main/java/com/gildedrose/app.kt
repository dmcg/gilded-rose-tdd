package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.Stock
import com.gildedrose.persistence.StockFileItems
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.pricing.valueElfClient
import dev.forkhandles.result4k.Result
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId

val stdOutAnalytics = loggingAnalytics(::println)
val londonZoneId = ZoneId.of("Europe/London")

data class App(
    val port: Int = 80,
    val stockFile: File = File("stock.tsv"),
    val features: Features = Features(),
    val valueElfUri: URI = URI.create("http://value-elf.com:8080/prices"),
    val clock: () -> Instant = Instant::now,
    val analytics: Analytics = stdOutAnalytics
) {
    private val stock = Stock(
        StockFileItems(stockFile),
        londonZoneId,
        itemUpdate = Item::updatedBy
    )
    private val pricedLoader = PricedStockListLoader(
        loading = { stock.stockList(it) },
        pricing = valueElfClient(valueElfUri),
        analytics = analytics
    )
    val routes = routesFor(
        clock = clock,
        analytics = analytics,
        features
    ) { loadStockList(it) }
    private val server = serverFor(port = port, routes)

    context(IO) fun loadStockList(now: Instant = clock()): Result<StockList, StockListLoadingError> =
        pricedLoader.load(now)

    fun start() {
        server.start()
    }
}
