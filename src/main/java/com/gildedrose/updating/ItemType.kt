package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
import com.gildedrose.domain.subtract
import java.time.LocalDate

abstract class ItemType {
    abstract fun update(item: Item, localDate: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: String): ItemType =
    when {
        sellByDate == null -> Undated()
        name.contains("Aged Brie", ignoreCase = true) -> Brie()
        name.contains("Backstage Pass", ignoreCase = true) -> Pass()
        name.startsWith("Conjured", ignoreCase = true) -> Conjured()
        else -> Standard()
    }

class Standard : ItemType() {
    override fun update(item: Item, localDate: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            localDate.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = subtract(item.quality, degradation))
    }
}

class Undated : ItemType() {
    override fun update(item: Item, localDate: LocalDate): Item {
        return item
    }
}

class Brie : ItemType() {
    override fun update(item: Item, localDate: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val improvement = when {
            localDate.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = item.quality + improvement)
    }
}

class Pass : ItemType() {
    override fun update(item: Item, localDate: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val daysUntilSellBy = item.sellByDate.toEpochDay() - localDate.toEpochDay()
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

class Conjured : ItemType() {
    override fun update(item: Item, localDate: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            localDate.isAfter(item.sellByDate) -> 4
            else -> 2
        }
        return item.copy(quality = subtract(item.quality, degradation))
    }
}

