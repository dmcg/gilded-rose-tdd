package com.gildedrose

import routesFor
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

class Fixture(
    initialStockList: StockList,
    val now: LocalDate = oct29,
    val stockFile: File = Files.createTempFile("stock", ".tsv").toFile()
) {

    init {
        save(initialStockList)
    }

    val routes = routesFor(stockFile) { now }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList {
        return stockFile.loadItems()
    }
}
