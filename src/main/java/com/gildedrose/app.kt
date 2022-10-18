package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.http.serverFor
import java.io.File
import java.time.Instant

val stdOutAnalytics = loggingAnalytics(::println)

data class App(
    val port: Int = 8080,
    val stockFile: File = File("stock.tsv"),
    val features: Features = Features(),
    val pricing: (Item) -> Price? = ::noOpPricing,
    val clock: () -> Instant = Instant::now,
    val analytics: Analytics = stdOutAnalytics
) {
    val routes = routesFor(
        stockFile = stockFile,
        clock = clock,
        pricing = pricing,
        analytics = analytics,
        features
    )
    private val server = serverFor(port = port, routes)

    fun start() {
        server.start()
    }
}

@Suppress("UNUSED_PARAMETER")
fun noOpPricing(item: Item): Price? = null
