package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.ResponseErrors
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

val analytics = loggingAnalytics(::println)

private val londonZoneId = ZoneId.of("Europe/London")

fun routesFor(
    stockFile: File,
    clock: () -> Instant,
    pricing: (Item) -> Price?,
    analytics: Analytics,
    features: Features,
): HttpHandler {
    val stock = Stock(stockFile, londonZoneId, Item::updatedBy)
    return ServerFilters.RequestTracing()
        .then(reportHttpTransactions(analytics))
        .then(catchAll(analytics))
        .then(ResponseErrors.reportTo(analytics))
        .then(routes(
            "/" bind Method.GET to listHandler(
                clock = clock,
                zoneId = londonZoneId,
                pricing = pricing,
                isPricingEnabled = features.pricing,
                listing = stock::stockList
            ),
            "/error" bind Method.GET to { error("deliberate") }
        )
    )
}

