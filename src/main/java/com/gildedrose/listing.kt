package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.magic
import com.gildedrose.foundation.runIO
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.rendering.render
import dev.forkhandles.result4k.Result4k
import org.http4k.core.HttpHandler
import java.time.Instant
import java.time.ZoneId

fun listHandler(
    clock: () -> Instant,
    zoneId: ZoneId,
    listing: context(IO) (Instant) -> Result4k<StockList, StockListLoadingError>,
    features: Features
): HttpHandler = { _ ->
    runIO {
        val now = clock()
        val stockListResult = listing(magic(), now)
        render(stockListResult, now, zoneId, features)
    }
}

