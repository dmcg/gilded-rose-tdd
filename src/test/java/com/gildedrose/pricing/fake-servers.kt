package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import org.http4k.routing.reverseProxy

/**
 * Starts a fake server for demos
 */
fun main() {
    val routes = reverseProxy(
        "value-elf.com" to fakeValueElfRoutes(pricingWithMultiplier(100)),
        "priceomatic.com" to fakeValueElfRoutes(pricingWithMultiplier(99)),
        "webuyanymagicalitem.com" to fakeValueElfRoutes(pricingWithMultiplier(101)),
    )

    serverFor(8080, routes).start()
}

private fun pricingWithMultiplier(multiplier: Int): (ID<Item>, Quality) -> Price? = { id, quality ->
    when {
        id.toString() == "no-such" -> null
        else -> Price(id.value.length * multiplier.toLong() + quality.valueInt)!!
    }
}
