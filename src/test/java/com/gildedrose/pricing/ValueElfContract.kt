package com.gildedrose.pricing

import com.gildedrose.domain.Price
import com.gildedrose.testItem
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate.now

val anItem = testItem("banana", "doesn't matter", now(), 9)
val aNotFoundItem = testItem("no-such", "doesn't matter", now(), 9)

abstract class ValueElfContract(
    val uri: URI,
    val handler: HttpHandler
) {
    val client = valueElfClient(uri, handler)

    @Test
    fun `returns price when there is one`() {
        assertEquals(Price(609), client(anItem))
    }

    @Test
    fun `returns null when no price`() {
        assertEquals(null, client(aNotFoundItem))
    }

    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val baseRequest = Request(Method.GET, uri.toString())
        assertThat(handler(baseRequest), hasStatus(Status.BAD_REQUEST))
        assertThat(handler(baseRequest.query("id", "some-id")), hasStatus(Status.BAD_REQUEST))
        assertThat(handler(baseRequest.query("id", "")), hasStatus(Status.BAD_REQUEST))
        assertThat(handler(baseRequest.query("id", "some-id").query("quality", "")), hasStatus(Status.BAD_REQUEST))
        assertThat(handler(baseRequest.query("id", "some-id").query("quality", "nan")), hasStatus(Status.BAD_REQUEST))
        assertThat(handler(baseRequest.query("id", "some-id").query("quality", "-1")), hasStatus(Status.BAD_REQUEST))
    }
}
