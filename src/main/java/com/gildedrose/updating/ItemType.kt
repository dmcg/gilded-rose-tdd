package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
import java.time.LocalDate

fun interface ItemType {
    operator fun invoke(item: Item, on: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: String): ItemType =
    when {
        sellByDate == null -> undated
        name.contains("Aged Brie", ignoreCase = true) -> Brie()
        name.contains("Backstage Pass", ignoreCase = true) -> Pass()
        name.startsWith("Conjured", ignoreCase = true) -> Conjured()
        else -> Standard
    }

object Standard : ItemType {
    override fun invoke(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            on.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = item.quality - degradation)
    }
}

val undated = ItemType { item, _ -> item }

class Brie : ItemType {
    override fun invoke(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val improvement = when {
            on.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = item.quality + improvement)
    }
}

class Pass : ItemType {
    override fun invoke(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val daysUntilSellBy = item.sellByDate.toEpochDay() - on.toEpochDay()
        return if (daysUntilSellBy < 0) {
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
}

class Conjured : ItemType {
    override fun invoke(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            on.isAfter(item.sellByDate) -> 4
            else -> 2
        }
        return item.copy(quality = item.quality - degradation)
    }
}

