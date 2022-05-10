package com.gildedrose

import java.time.LocalDate

abstract class Item {
    abstract val name: String
    abstract val quality: Int
    abstract fun updated(on: LocalDate): Item
    abstract fun withQuality(quality: Int): Item

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this) { item, date -> item.updated(on = date) }
    }
}

fun Item(
    name: String,
    sellByDate: LocalDate?,
    quality: Int
) = when (sellByDate) {
    null -> UndatedItem(name, quality)
    else -> DatedItem(name, sellByDate, quality)
}

data class UndatedItem(
    override val name: String,
    override val quality: Int
) : Item() {
    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }
    override fun updated(on: LocalDate) = this
    override fun withQuality(quality: Int) = this.copy(quality = quality)
}

data class DatedItem(
    override val name: String,
    val sellByDate: LocalDate,
    override val quality: Int
) : Item() {
    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    override fun updated(on: LocalDate): DatedItem {
        val degradation = when {
            on.isAfter(sellByDate) -> 2
            else -> 1
        }
        return withQuality(quality = (quality - degradation).coerceAtLeast(0))
    }

    override fun withQuality(quality: Int) = this.copy(quality = quality)
}
