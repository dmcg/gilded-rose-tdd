package com.gildedrose.domain

import java.time.LocalDate

@Suppress("DataClassPrivateConstructor") // protected by requires in init
data class Item private constructor(
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: NonNegativeInt,
    private val type: ItemType
) {
    constructor(
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: NonNegativeInt,
    ): this(name, sellByDate, quality, typeFor(sellByDate, name))

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }

    fun withQuality(quality: Int): Item {
        val qualityCap = this.quality.value.coerceAtLeast(50)
        return copy(
            quality = NonNegativeInt(quality.coerceIn(0, qualityCap)) ?: error("tried to create a negative int")
        )
    }
}

