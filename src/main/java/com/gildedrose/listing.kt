package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.rendering.render
import dev.forkhandles.result4k.Result4k
import org.http4k.core.HttpHandler
import java.time.Instant
import java.time.ZoneId

fun listHandler(
    clock: () -> Instant,
    zoneId: ZoneId,
    listing: (Instant) -> Result4k<StockList, StockListLoadingError>
): HttpHandler = { _ ->
    val now = clock()
    val stockListResult = listing(now)
    render(stockListResult, now, zoneId)
}

