package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.then
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    initialStockList: StockList,
    val now: Instant,
    val events: MutableList<Any> = mutableListOf(),
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile()
) {
    init {
        save(initialStockList)
    }

    val routes = routesFor(
        stockFile = stockFile,
        clock = { now },
        analytics = analytics then { events.add(it) }
    )

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList {
        return stockFile.loadItems() ?: error("Could not load stock")
    }
}

