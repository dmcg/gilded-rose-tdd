package com.gildedrose

import com.gildedrose.config.DbConfig
import com.gildedrose.config.Features
import com.gildedrose.config.toDslContext
import com.gildedrose.domain.*
import com.gildedrose.foundation.*
import com.gildedrose.persistence.*
import com.gildedrose.pricing.PricedStockListLoader
import com.gildedrose.pricing.valueElfClient
import com.gildedrose.updating.Stock
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

    private val stock = Stock(items, londonZoneId)

    private val pricedLoader = PricedStockListLoader(
        loading = { stock.loadAndUpdateStockList(it) },
        pricing = pricing,
        analytics = analytics
    )

    context(IO)
    fun loadStockList(now: Instant = clock()): Result<PricedStockList, StockListLoadingError> = result4k {
        items.inTransaction {
            pricedLoader.load(now)
        }
    }

    context(IO)
    fun deleteItemsWithIds(itemIds: Set<ID<Item>>, now: Instant = clock()): Unit = ignoreErrors {
        items.inTransaction {
            val stockList = stock.loadAndUpdateStockList(now)
            val newItems = stockList.items.filterNot { it.id in itemIds }
            if (newItems != stockList.items) {
                items.save(StockList(now, newItems))
            }
        }
    }

    context(IO)
    fun addItem(newItem: Item, now: Instant = clock()): Unit = ignoreErrors {
        items.inTransaction {
            val newItems = stock.loadAndUpdateStockList(now).items + newItem
            items.save(StockList(now, newItems))
        }
    }
}
