@file:JvmName("🙈")
package com.gildedrose.ignoreme

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverOn
import com.gildedrose.pricing.fakeValueElfRoutes
import org.http4k.routing.reverseProxy

/**
 * Starts a fake com.gildedrose.server for demos
 */
fun main() {
    val routes = reverseProxy(
        "localhost" to fakeValueElfRoutes(pricingWithMultiplier(1000)),
        "value-elf.com" to fakeValueElfRoutes(pricingWithMultiplier(1000)),
        "priceomatic.com" to fakeValueElfRoutes(pricingWithMultiplier(990)),
        "webuyanymagicalitem.com" to fakeValueElfRoutes(pricingWithMultiplier(1010)),
    )

    routes.serverOn(8080).start()
}

private fun pricingWithMultiplier(multiplier: Int): (ID<Item>, Quality) -> Price? = { id, quality ->
    Thread.sleep(100)
    when {
        id.value.value == "banana" -> Price(709)!!
        id.toString() == "no-such" -> null
        else -> Price((id.value.length + 1 + quality.valueInt) * multiplier.toLong())!!
    }
}
