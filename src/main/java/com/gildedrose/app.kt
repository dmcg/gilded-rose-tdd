package com.gildedrose

import com.gildedrose.config.DbConfig
import com.gildedrose.config.Features
import com.gildedrose.config.toDslContext
import com.gildedrose.domain.*
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.persistence.DbItems
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import com.gildedrose.pricing.PricedStockListLoader
import com.gildedrose.pricing.valueElfClient
import com.gildedrose.updating.Stock
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
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
    val pricing: (Item) -> Price?
) {
    constructor(
        dbConfig: DbConfig,
        features: Features = Features(),
        valueElfUri: URI = URI.create("http://value-elf.com:8080/prices"),
        clock: () -> Instant = Instant::now,
        analytics: Analytics = stdOutAnalytics
    ) : this(
        DbItems(dbConfig.toDslContext()),
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

    fun loadStockList(now: Instant = clock()): Result<PricedStockList, StockListLoadingError> =
        items.inTransaction {
            pricedLoader.load(now)
        }

    fun deleteItemsWithIds(itemIds: Set<ItemID>, now: Instant = clock()) {
        items.inTransaction {
            stock.loadAndUpdateStockList(now).map { stockList ->
                val newItems = stockList.items.filterNot { it.id in itemIds }
                if (newItems != stockList.items) {
                    items.save(StockList(now, newItems))
                }
            }
        }
    }

    fun addItem(newItem: Item, now: Instant = clock()) {
        items.inTransaction {
            stock.loadAndUpdateStockList(now).map { stockList ->
                val newItems = stockList.items + newItem
                items.save(StockList(now, newItems))
            }
        }
    }
}


