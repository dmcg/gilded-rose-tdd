package com.gildedrose

import com.gildedrose.domain.updateItems
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.LoggingAnalytics
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.persistence.Stock
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.File
import java.time.Instant
import java.time.ZoneId

val analytics = LoggingAnalytics(::println)

private val londonZoneId = ZoneId.of("Europe/London")

fun routesFor(
    stockFile: File,
    clock: () -> Instant,
    analytics: Analytics
): HttpHandler {
    val stock = Stock(stockFile, londonZoneId, ::updateItems)
    return ServerFilters.RequestTracing().then(
        reportHttpTransactions(analytics).then(
            catchAll(analytics).then(
                routes(
                    "/" bind Method.GET to listHandler(clock, londonZoneId, stock::stockList),
                    "/error" bind Method.GET to { error("deliberate") }
                )
            )
        )
    )
}

