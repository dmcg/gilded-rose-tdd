package com.gildedrose.domain

import java.time.LocalDate

@Suppress("DataClassPrivateConstructor") // protected by requires in init
data class Item private constructor(
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Int,
    private val type: ItemType
) {
    companion object {
        operator fun invoke(
            name: String,
            sellByDate: LocalDate?,
            quality: Int,
        ): Item? = try {
            Item(name, sellByDate, quality, typeFor(sellByDate, name))
        } catch (x: Exception) {
            null
        }
    }

    init {
        require(quality >= 0) {
            "Quality is $quality but should not be negative"
        }
    }

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }

    fun withQuality(quality: Int): Item {
        val qualityCap = this.quality.coerceAtLeast(50)
        return copy(quality = quality.coerceIn(0, qualityCap))
    }
}


