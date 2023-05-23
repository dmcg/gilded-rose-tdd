package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.runIO
import com.gildedrose.foundation.then
import com.gildedrose.persistence.saveTo
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
        runIO {
            stockList.saveTo(stockFile)
        }
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

