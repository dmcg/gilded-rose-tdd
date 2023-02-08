package com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.item
import com.gildedrose.londonZoneId
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

class StockEventsTests {

    val aTimestamp = Instant.parse("2023-02-08T12:00:00Z")
    val sameTimeAsTimestamp = ZonedDateTime.ofInstant(aTimestamp, londonZoneId)

    @Test
    fun `no events returns no Items`() {
        expectThat(
            emptyList<StockEvent>().toItems(sameTimeAsTimestamp)
        ).isEmpty()
    }

    @Test
    fun `StockAdded events create Items`() {
        expectThat(
            listOf(
                stockAdded(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAdded(aTimestamp, "id-2", "another name", null, 99)
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                item("id-2", "another name", null, 99)
            )
        )
    }

    @Test
    fun `StockAdded events can create items with the same id`() {
        expectThat(
            listOf(
                stockAdded(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAdded(aTimestamp, "id-1", "another name", null, 99)
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                item("id-1", "another name", null, 99)
            )
        )
    }

    @Test
    fun `StockRemoved events remove Items`() {
        expectThat(
            listOf(
                stockAdded(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAdded(aTimestamp, "id-2", "another name", null, 99),
                stockRemoved("id-2")
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 2, 8), 42),
            )
        )
    }

    @Test
    fun `StockRemoved events are ignored if no item exists`() {
        expectThat(
            listOf(
                stockAdded(aTimestamp, "id-1", "name", LocalDate.of(2023, 2, 8), 42),
                stockAdded(aTimestamp, "id-2", "another name", null, 99),
                stockRemoved("no-such")
            ).toItems(sameTimeAsTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 2, 8), 42),
                item("id-2", "another name", null, 99),
            )
        )
    }

    @Test
    fun `items are degraded the day after the added event`() {
        val addedTimestamp = Instant.parse("2023-02-08T12:00:00Z")

        val dayOfAddedTimestamp = ZonedDateTime.parse("2023-02-08T23:59:59Z")
        expectThat(
            listOf(
                stockAdded(addedTimestamp, "id-1", "name", LocalDate.of(2023, 3, 8), 42),
            ).toItems(dayOfAddedTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 3, 8), 42)
            )
        )

        val dayAfterAddedTimestamp = ZonedDateTime.parse("2023-02-09T00:00:00Z")
        expectThat(
            listOf(
                stockAdded(addedTimestamp, "id-1", "name", LocalDate.of(2023, 3, 8), 42),
            ).toItems(dayAfterAddedTimestamp)
        ).isEqualTo(
            listOf(
                item("id-1", "name", LocalDate.of(2023, 3, 8), 41)
            )
        )
    }
}

private fun stockAdded(timestamp: Instant, id: String, name: String, sellByDate: LocalDate?, quality: Int) =
    StockAdded(
        timestamp,
        ID(id)!!,
        NonBlankString(name)!!,
        sellByDate,
        Quality(quality)!!
    )

private fun stockRemoved(id: String) = StockRemoved(ID(id)!!)



