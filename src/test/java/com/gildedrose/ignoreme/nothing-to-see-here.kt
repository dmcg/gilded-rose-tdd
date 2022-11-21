@file:JvmName("🙈")
package com.gildedrose.ignoreme

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.pricing.fakeValueElfRoutes
import org.http4k.routing.reverseProxy
import kotlin.random.Random

/**
 * Starts a fake com.gildedrose.server for demos
 */
fun main() {
    val routes = reverseProxy(
        "value-elf.com" to fakeValueElfRoutes(pricingWithMultiplier(1000)),
        "priceomatic.com" to fakeValueElfRoutes(pricingWithMultiplier(990)),
        "webuyanymagicalitem.com" to fakeValueElfRoutes(pricingWithMultiplier(1010)),
    )

    serverFor(8080, routes).start()
}

private fun pricingWithMultiplier(multiplier: Int): (ID<Item>, Quality) -> Price? = { id, quality ->
    Thread.sleep(50)
    when {
        Random.nextDouble() > 0.95 -> error("Random failure")
        id.value.value == "banana" -> Price(709)!!
        id.toString() == "no-such" -> null
        else -> Price((id.value.length + 1 + quality.valueInt) * multiplier.toLong())!!
    }
}
