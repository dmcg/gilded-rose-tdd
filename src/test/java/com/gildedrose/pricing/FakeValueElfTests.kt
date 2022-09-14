package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI

private val aUri: URI = URI.create("http://localhost:8888/prices")

private val priceLookup: Map<Pair<ID<Item>, Quality>, Price?> = mapOf(
    (anItem.id to anItem.quality) to Price(609),
    (aNotFoundItem.id to aNotFoundItem.quality) to null,
)
private val routes: HttpHandler = fakeValueElfRoutes { id, quality ->
    priceLookup[id to quality]
}

class FakeValueElfTests : ValueElfContract(
    uri = aUri,
    handler = routes
)

@EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
class FakeValueElfHttpTests : ValueElfContract(
    uri = aUri,
    handler = ApacheClient()
) {
    companion object {
        val server = fakeValueElfServer(8888) { id, quality ->
            priceLookup[id to quality]
        }

        @BeforeAll @JvmStatic
        fun startServer() {
            server.start()
        }

        @AfterAll @JvmStatic
        fun stopServer() {
            server.stop()
        }
    }
}

@Suppress("SameParameterValue")
private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(
    port,
    fakeValueElfRoutes(pricing)
)


