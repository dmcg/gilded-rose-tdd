package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
import com.gildedrose.domain.minus
import com.gildedrose.domain.plus
import java.time.LocalDate

fun interface ItemType {
    fun update(item: Item, on: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: String): ItemType {
    val baseType = when {
        sellByDate == null -> UndatedType
        name.contains("Aged Brie", ignoreCase = true) -> BrieType
        name.contains("Backstage Pass", ignoreCase = true) -> PassType
        else -> StandardType
    }
    return when {
        name.startsWith("Conjured", ignoreCase = true) -> conjured(baseType)
        else -> baseType
    }
}

fun conjured(baseType: ItemType) = typeFor("CONJURED $baseType") { item, on ->
    val updated = baseType.update(item, on)
    val change = item.quality.value - updated.quality.value
    item.copy(quality = item.quality - 2 * change)
}

val StandardType = typeFor("STANDARD") { item, on ->
    requireNotNull(item.sellByDate)
    val degradation = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.copy(quality = item.quality - degradation)
}

val UndatedType = typeFor("UNDATED") { item, _ -> item }

val BrieType = typeFor("BRIE") { item, on ->
    requireNotNull(item.sellByDate)
    val improvement = when {
        on.isAfter(item.sellByDate) -> 2
        else -> 1
    }
    item.copy(quality = item.quality + improvement)
}

val PassType = typeFor("PASS") { item, on ->
    requireNotNull(item.sellByDate)
    val daysUntilSellBy = item.sellByDate.toEpochDay() - on.toEpochDay()
    if (daysUntilSellBy < 0) {
        item.copy(quality = Quality.ZERO)
    } else {
        val improvement = when {
            daysUntilSellBy < 5 -> 3
            daysUntilSellBy < 10 -> 2
            else -> 1
        }
        item.copy(quality = item.quality + improvement)
    }
}

private fun typeFor(name: String, updater: ItemType) =
    object : ItemType by updater {
        override fun toString() = name
        override fun equals(other: Any?): Boolean {
            if (other !is ItemType) return false
            return this.toString() == other.toString()
        }
    }
