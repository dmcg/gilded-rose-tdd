package com.gildedrose.domain

import java.time.LocalDate

data class Item(
    val id: ID<Item>?,
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
    private val type: ItemType
) {
    constructor(
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: Quality,
    ): this(null, name, sellByDate, quality, typeFor(sellByDate, name))

    constructor(
        id: ID<Item>,
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: Quality,
    ): this(id, name, sellByDate, quality, typeFor(sellByDate, name))

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }
}

