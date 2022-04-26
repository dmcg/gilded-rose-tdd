package com.gildedrose

import java.time.LocalDate

data class Item(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int
) {
    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong())}
        return dates.fold(this) { item, date -> item.update(date)}
    }

    private fun update(on: LocalDate): Item {
        val degradation = when {
            sellByDate == null -> 0
            on.isAfter(sellByDate) -> 2
            else -> 1
        }
        return copy(quality = (quality - degradation).coerceAtLeast(0))
    }

}
