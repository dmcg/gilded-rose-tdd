package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
import java.time.LocalDate

abstract class ItemType {
    abstract fun update(item: Item, on: LocalDate): Item
}

fun typeFor(sellByDate: LocalDate?, name: String): ItemType {
    return when {
        sellByDate == null -> Undated()
        name.contains("Aged Brie", ignoreCase = true) -> Brie()
        name.contains("Backstage Pass", ignoreCase = true) -> Pass()
        name.startsWith("Conjured", ignoreCase = true) -> Conjured()
        else -> Standard()
    } }

class Conjured : ItemType() {
    override fun update(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            on.isAfter(item.sellByDate) -> 4
            else -> 2
        }
        return item.copy(quality = subtract(item.quality, degradation))
    }

}

class Standard : ItemType() {
    override fun update(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val degradation = when {
            on.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = subtract(item.quality, degradation))
    }
}

class Undated : ItemType() {
    override fun update(item: Item, on: LocalDate): Item {
        return item
    }
}

class Brie : ItemType() {
    override fun update(item: Item, on: LocalDate): Item {
        requireNotNull(item.sellByDate)
        val improvement = when {
            on.isAfter(item.sellByDate) -> 2
            else -> 1
        }
        return item.copy(quality = add(item.quality, improvement))
    }

}

class Pass : ItemType() {
    override fun update(item: Item, on: LocalDate): Item {
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
            item.copy(quality = add(item.quality, improvement))
        }
    }

}

fun subtract(quality: Quality, value: Int): Quality =
    add(quality, -value)

fun add(quality: Quality, value: Int): Quality {
    val qualityCap = quality.value.value.coerceAtLeast(50)
    return Quality((quality.value + value).coerceIn(0, qualityCap))
        ?: error("tried to create a negative int")
}
