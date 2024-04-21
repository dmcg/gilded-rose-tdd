package com.gildedrose.persistence.eventSource

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.Quality
import com.gildedrose.updating.daysTo
import com.gildedrose.updating.updatedBy
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

sealed class StockEvent

data class StockAdded(
    val timestamp: Instant,
    val id: ID<Item>,
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Quality,
) : StockEvent()

data class StockRemoved(
    val id: ID<Item>,
) : StockEvent()

fun Iterable<StockEvent>.toItems(now: ZonedDateTime): List<Item> {
    val result = mutableListOf<Item>()
    for (event in this) {
        when (event) {
            is StockAdded -> {
                val itemWhenAdded = event.toItem()
                val daysSinceAdded = event.timestamp.daysTo(now.toInstant(), now.zone)
                result.add(itemWhenAdded.updatedBy(daysSinceAdded.toInt(), now.toLocalDate()))
            }
            is StockRemoved -> result.removeIf { it.id == event.id }
        }
    }
    return result
}

private fun StockAdded.toItem() = Item(id, name, sellByDate, quality)
