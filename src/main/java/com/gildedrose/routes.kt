package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.ResponseErrors
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.persistence.Stock
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.resultFrom
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.then
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
    val listing: (Instant) -> Result<StockList, StockListLoadingError> = { now: Instant ->
        stock.stockList(now).map { it.pricedBy(pricing) }
    }
    return ServerFilters.RequestTracing()
        .then(reportHttpTransactions(analytics))
        .then(catchAll(analytics))
        .then(ResponseErrors.reportTo(analytics))
        .then(routes(
            "/" bind Method.GET to listHandler(
                clock = clock,
                zoneId = londonZoneId,
                isPricingEnabled = features.pricing,
                listing = listing
            ),
            "/error" bind Method.GET to { error("deliberate") }
        )
    )
}

private fun StockList.pricedBy(pricing: (Item) -> Price?): StockList =
    this.copy(items = items.map { it.pricedBy(pricing)})

private fun Item.pricedBy(pricing: (Item) -> Price?): Item =
    this.copy(price = resultFrom { pricing(this) })

