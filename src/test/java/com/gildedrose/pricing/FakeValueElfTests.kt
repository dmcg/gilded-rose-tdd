package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import org.http4k.client.ApacheClient
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI

private val baseFixture = ValueElfContract.Fixture(
    uri = URI.create("http://localhost:8888/prices"),
    handler = { Response(Status.I_M_A_TEAPOT) },
    expectedPrice = Price(609)!!
)

private val priceLookup: Map<Pair<ID<Item>, Quality>, Price?> =
    with(baseFixture) {
        mapOf(
            (aFoundItem.id to aFoundItem.quality) to expectedPrice,
            (aNotFoundItem.id to aNotFoundItem.quality) to null,
        )
    }

class FakeValueElfTests : ValueElfContract(
    baseFixture.copy(handler = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    })
)

@EnabledIfSystemProperty(named = "run-external-tests", matches = "true")
class FakeValueElfHttpTests : ValueElfContract(
    baseFixture.copy(handler = ApacheClient())
) {
    companion object {
        val server = serverFor(
            port = 8888,
            fakeValueElfRoutes { id: ID<Item>, quality: Quality ->
                priceLookup[id to quality]
            }
        )

        @BeforeAll
        @JvmStatic
        fun startServer() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            server.stop()
        }
    }
}


