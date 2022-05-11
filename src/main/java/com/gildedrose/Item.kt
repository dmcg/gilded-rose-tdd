package com.gildedrose

import java.time.LocalDate

open class Item(
    val name: String,
    val quality: Int
) {
    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    open fun updated(on: LocalDate) = this
    open fun withQuality(quality: Int) = Item(quality = quality, name = name)

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this) { item, date -> item.updated(on = date) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (name != other.name) return false
        if (quality != other.quality) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + quality
        return result
    }

}

fun Item(
    name: String,
    sellByDate: LocalDate?,
    quality: Int
) = when (sellByDate) {
    null -> Item(name, quality)
    else -> DatedItem(name, sellByDate, quality)
}

class DatedItem(
    name: String,
    val sellByDate: LocalDate,
    quality: Int
) : Item(name, quality) {

    override fun updated(on: LocalDate): DatedItem {
        val degradation = when {
            on.isAfter(sellByDate) -> 2
            else -> 1
        }
        return withQuality(quality = (quality - degradation).coerceAtLeast(0))
    }

    override fun withQuality(quality: Int) = DatedItem(quality = quality, name = name, sellByDate = sellByDate)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DatedItem

        if (sellByDate != other.sellByDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sellByDate.hashCode()
        return result
    }


}
