package com.gildedrose

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.foundation.runIO
import com.gildedrose.http.ResponseErrors
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.rendering.render
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Duration


val App.routes: HttpHandler
    get() = ServerFilters.RequestTracing()
        .then(reportHttpTransactions(Duration.ofSeconds(1), analytics))
        .then(catchAll(analytics))
        .then(ResponseErrors.reportTo(analytics))
        .then(
            routes(
                "/" bind Method.GET to ::listHandler,
                "/error" bind Method.GET to { error("deliberate") },
                "/delete-items" bind Method.POST to ::deleteHandler
            )
        )

private fun App.listHandler(
    @Suppress("UNUSED_PARAMETER") request: Request
): Response =
    runIO {
        val now = this.clock()
        val stockListResult = loadStockList(now)
        render(stockListResult, now, londonZoneId, this.features)
    }

private fun App.deleteHandler(
    request: Request
): Response {
    println(request.form().map { it.first }.map { ID<Item>(it) })
    return Response(Status.SEE_OTHER).header("Location", "/")
}

