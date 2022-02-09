package com.gildedrose

import routesFor
import java.io.File
import java.nio.file.Files

class Fixture(
    initialStockList: StockList,
    private val stockFile: File = Files.createTempFile("stock", ".tsv").toFile()
) {

    init {
        save(initialStockList)
    }

    val routes = routesFor(stockFile) { oct29 }

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }
}
