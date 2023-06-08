package com.gildedrose

import com.gildedrose.config.DbConfig
import com.gildedrose.config.toDslContext
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.persistence.*
import com.gildedrose.pricing.valueElfClient
import dev.forkhandles.result4k.Result
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId

val stdOutAnalytics = loggingAnalytics(::println)
val londonZoneId = ZoneId.of("Europe/London")

data class App(
    val items: Items<TXContext>,
    val features: Features = Features(),
    val clock: () -> Instant = Instant::now,
    val analytics: Analytics = stdOutAnalytics,
    val pricing: context(IO) (Item) -> Price?
) {
    constructor(
        stockFile: File = File("stock.tsv"),
        dbConfig: DbConfig,
        features: Features = Features(),
        valueElfUri: URI = URI.create("http://value-elf.com:8080/prices"),
        clock: () -> Instant = Instant::now,
        analytics: Analytics = stdOutAnalytics
    ) : this(
        DualItems(StockFileItems(stockFile), DbItems(dbConfig.toDslContext()), analytics),
        features,
        clock,
        analytics,
        valueElfClient(valueElfUri)
    )

    private val stock = Stock(
        items,
        londonZoneId,
        itemUpdate = Item::updatedBy
    )
    private val pricedLoader = PricedStockListLoader(
        loading = { stock.stockList(it) },
        pricing = pricing,
        analytics = analytics
    )

    context(IO) fun loadStockList(now: Instant = clock()): Result<StockList, StockListLoadingError> =
        pricedLoader.load(now)

}
