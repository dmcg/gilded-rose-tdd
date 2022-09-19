package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.http.serverFor
import java.io.File
import java.time.Instant

fun server(
    port: Int = 8080,
    stockFile: File = File("stock.tsv"),
    features: Features = Features(),
    pricing: (Item) -> Price? = ::noOpPricing
) = serverFor(
    port = port,
    routesFor(
        stockFile = stockFile,
        clock = { Instant.now() },
        pricing = pricing,
        analytics = analytics,
        features
    )
)

@Suppress("UNUSED_PARAMETER")
fun noOpPricing(item: Item): Price? = null
