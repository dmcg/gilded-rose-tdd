package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.IO
import com.gildedrose.http.ResponseErrors
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Duration
import java.time.Instant


fun routesFor(
    clock: () -> Instant,
    analytics: Analytics,
    @Suppress("UNUSED_PARAMETER") features: Features,
    listing: context(IO) (Instant) -> Result<StockList, StockListLoadingError>,
): HttpHandler =
    ServerFilters.RequestTracing()
        .then(reportHttpTransactions(Duration.ofSeconds(1), analytics))
        .then(catchAll(analytics))
        .then(ResponseErrors.reportTo(analytics))
        .then(
            routes(
                "/" bind Method.GET to listHandler(
                    clock = clock,
                    zoneId = londonZoneId,
                    listing = listing
                ),
                "/error" bind Method.GET to { error("deliberate") }
            )
        )


