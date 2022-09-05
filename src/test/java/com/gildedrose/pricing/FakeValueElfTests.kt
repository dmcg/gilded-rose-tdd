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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

class FakeValueElfTests {

    val uri: URI = URI.create("http://localhost:8888/prices")
    val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
    val priceLookup: MutableMap<Pair<ID<Item>, Quality>, Price?> = mutableMapOf()
    val routes = fakeValueElfRoutes { id, quality ->
        priceLookup[id to quality]
    }
    val client = valueElfClient(uri, routes)

    @Disabled("slows tests")
    @Test
    fun `actually call server`() {
        priceLookup[(item.id to item.quality)] = Price(609)
        val server = fakeValueElfServer(8888) { id, quality ->
            priceLookup[id to quality]
        }
        val client: (Item) -> Price? = valueElfClient(uri)
        server.start().use {
            assertEquals(Price(609), client.invoke(item))
        }
    }

    @Test
    fun `returns price that does exist`() {
        priceLookup[(item.id to item.quality)] = Price(609)
        assertEquals(Price(609), client.invoke(item))
    }

    @Test
    fun `returns null for no price`() {
        assertEquals(null, client.invoke(item))
    }

    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(Method.GET, uri.toString())
        assertThat(routes(baseRequest), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "nan")), hasStatus(BAD_REQUEST))
        assertThat(routes(baseRequest.query("id", "some-id").query("quality", "-1")), hasStatus(BAD_REQUEST))
    }
}

/**
 * Starts a fake server for demos
 */
fun main() {
    fakeValueElfServer(8080) { id, quality ->
        when {
            id.toString() == "no-such" -> null
            else -> Price(id.value.length * 100L + quality.valueInt)!!
        }
    }.start()
}

private fun fakeValueElfServer(port: Int, pricing: (ID<Item>, Quality) -> Price?) = serverFor(port,
    fakeValueElfRoutes(pricing)
)


