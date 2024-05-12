@file:JvmName("🙈")
package com.gildedrose.ignoreme

import com.gildedrose.domain.ItemID
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.pricing.fakeValueElfRoutes
import org.http4k.routing.reverseProxy

/**
 * Starts a fake com.gildedrose.server for demos
 */
fun main() {
    runFakePricing()
}

fun runFakePricing() {
    val routes = reverseProxy(
        "localhost" to fakeValueElfRoutes(pricingWithMultiplier(1000)),
        "value-elf.com" to fakeValueElfRoutes(pricingWithMultiplier(1000)),
        "priceomatic.com" to fakeValueElfRoutes(pricingWithMultiplier(990)),
        "webuyanymagicalitem.com" to fakeValueElfRoutes(pricingWithMultiplier(1010)),
    )

    serverFor(8080, routes).start()
}

private fun pricingWithMultiplier(multiplier: Int): (ItemID, Quality) -> Price? = { id, quality ->
    Thread.sleep(100)
    when (id) {
        "banana" -> Price(709)!!
        "no-such" -> null
        else -> Price((id.length + 1 + quality.valueInt) * multiplier.toLong())!!
    }
}
