package com.gildedrose

import java.time.LocalDate

fun updateItems(items: List<Item>, days: Int, on: LocalDate) = items.map {
    it.updatedBy(days, on)
}

fun updaterFor(sellByDate: LocalDate?, name: String) = when {
    sellByDate == null -> Item::updateUndated
    name == "Aged Brie" -> Item::updateBrie
    name.startsWith("Backstage Pass") -> Item::updatePass
    else -> Item::updateStandard
}

@Suppress("UNUSED_PARAMETER")
private fun Item.updateUndated(on: LocalDate): Item = this

private fun Item.updateStandard(on: LocalDate): Item {
    requireNotNull(sellByDate)
    val degradation = when {
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return withQuality(quality - degradation)
}

private fun Item.updateBrie(on: LocalDate): Item {
    requireNotNull(sellByDate)
    val improvement = when {
        on.isAfter(sellByDate) -> 2
        else -> 1
    }
    return withQuality(quality + improvement)
}

private fun Item.updatePass(on: LocalDate): Item {
    requireNotNull(sellByDate)
    val daysUntilSellBy = sellByDate.toEpochDay() - on.toEpochDay()
    val newQuality = when {
        daysUntilSellBy < 0 -> 0
        daysUntilSellBy < 5 -> 3 + quality
        daysUntilSellBy < 10 -> 2 + quality
        else -> 1 + quality
    }
    return withQuality(newQuality)
}
