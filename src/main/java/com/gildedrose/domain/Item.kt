package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data
import dev.forkhandles.result4k.Result4k
import java.time.LocalDate

@Data
data class Item @Calculation constructor(
    val id: ID<Item>,
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>? = null,
    private val type: ItemType
) {
    @Calculation
    constructor(
        id: ID<Item>,
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: Quality,
    ): this(
        id,
        name,
        sellByDate,
        quality,
        type = typeFor(sellByDate, name)
    )

    @Calculation
    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }
}

