package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.then
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import dev.forkhandles.result4k.onFailure
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    initialStockList: StockList,
    val now: Instant,
    pricing: (Item) -> Price? = ::noOpPricing,
    val events: MutableList<Any> = mutableListOf(),
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
    features: Features = Features()
) {
    init {
        save(initialStockList)
    }

    val routes = routesFor(
        stockFile = stockFile,
        clock = { now },
        pricing = pricing,
        analytics = analytics then { events.add(it) },
        features = features
    )

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList {
        return stockFile.loadItems().onFailure { error("Could not load stock") }
    }
}

