package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.then
import com.gildedrose.persistence.loadItems
import com.gildedrose.persistence.saveTo
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Request
import org.http4k.core.Response
import java.io.File
import java.nio.file.Files
import java.time.Instant

class Fixture(
    val app: App,
    val events: MutableList<Any>
) {
    val stockFile get() = app.stockFile
    val routes: (Request) -> Response = app.routes

    fun save(stockList: StockList) {
        stockList.saveTo(stockFile)
    }

    fun load(): StockList {
        return stockFile.loadItems().onFailure { error("Could not load stock") }
    }
}

fun App.fixture(
    stockFile: File = Files.createTempFile("stock", ".tsv").toFile(),
    now: Instant,
    events: MutableList<Any> = mutableListOf(),
    initialStockList: StockList
) =
    Fixture(
        events = events,
        app = copy(
            stockFile = stockFile,
            clock = { now },
            analytics = analytics then { events.add(it) }
        )
    ).apply { save(initialStockList) }

