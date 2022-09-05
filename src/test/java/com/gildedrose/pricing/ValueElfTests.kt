package com.gildedrose.pricing

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.testItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

@Disabled("Talks to outside test resources, with results subject to change")
class ValueElfTests {
    val uri = URI.create("http://value-elf.com:8080/prices")
    val client: (Item) -> Price? = valueElfClient(uri)

    @Test
    fun `returns price when there is one`() {
        val item = testItem("banana", "doesn't matter", LocalDate.now(), 9)
        assertEquals(Price(609), client(item))
    }

    @Test
    fun `returns null when no price`() {
        val item = testItem("no-such", "doesn't matter", LocalDate.now(), 9)
        assertEquals(null, client(item))
    }
}

