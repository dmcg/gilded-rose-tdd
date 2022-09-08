package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.http.serverFor
import com.gildedrose.testItem
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI
import java.time.LocalDate

private val aUri: URI = URI.create("http://localhost:8888/prices")
private val anItem = testItem("banana", "doesn't matter", LocalDate.now(), 9)
private val aNotFoundItem = testItem("no-such", "doesn't matter", LocalDate.now(), 9)

class FakeValueElfTests {

    val priceLookup: Map<Pair<ID<Item>, Quality>, Price?> = mapOf(
        (anItem.id to anItem.quality) to Price(609),
        (aNotFoundItem.id to aNotFoundItem.quality) to null,
    )
    val routes = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    }
    val client = valueElfClient(aUri, routes)

    @Test
    fun `returns price that does exist`() {
        assertEquals(Price(609), client.invoke(anItem))
    }

    @Test
    fun `returns null for no price`() {
        assertEquals(null, client.invoke(aNotFoundItem))
    }

    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(Method.GET, aUri.toString())
        assertThat(routes(baseRequest), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "nan")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "-1")), hasStatus(BAD_REQUEST))
    }

    @EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
    @Test
    fun `actually call server`() {
        val server = fakeValueElfServer(8888) { id, quality ->
            priceLookup[id to quality]
        }
        val client: (Item) -> Price? = valueElfClient(aUri)
        server.start().use {
            assertEquals(Price(609), client.invoke(anItem))
        }
    }
}

@Suppress("SameParameterValue")
private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(
    port,
    fakeValueElfRoutes(pricing)
)


