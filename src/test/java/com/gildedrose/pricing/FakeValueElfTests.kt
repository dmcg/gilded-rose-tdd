package com.gildedrose.pricing

import com.gildedrose.domain.ItemID
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import org.http4k.core.Response
import org.http4k.core.Status
import java.net.URI

private val baseFixture = ValueElfContract.Fixture(
    uri = URI.create("http://localhost:8888/prices"),
    handler = { Response(Status.I_M_A_TEAPOT) },
    expectedPrice = Price(609)!!
)

private val priceLookup: Map<Pair<ItemID, Quality>, Price?> =
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
