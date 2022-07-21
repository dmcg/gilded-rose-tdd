package com.gildedrose.domain

import java.time.LocalDate

data class Item(
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
    private val type: ItemType
) {
    constructor(
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: Quality,
    ): this(name, sellByDate, quality, typeFor(sellByDate, name))

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }
}

