package com.gildedrose

import java.time.LocalDate

data class Item(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int
) {
    private val updater: (on: LocalDate) -> Item =
        if (this.name == "Aged Brie") this::updateBrie
        else this::updateStandard

    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this) { item, date -> item.updater(date) }
    }
}

private fun Item.updateStandard(on: LocalDate): Item {
    val degradation = when {
        sellByDate == null -> 0
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return copy(quality = (quality - degradation).coerceAtLeast(0))
}

private fun Item.updateBrie(on: LocalDate): Item {
    val improvement = when {
        sellByDate == null -> 0
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return copy(quality = (quality + improvement).coerceAtMost(50))
}
