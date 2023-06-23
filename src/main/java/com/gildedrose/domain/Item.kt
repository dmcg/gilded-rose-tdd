package com.gildedrose.domain

import dev.forkhandles.result4k.Result4k
import java.time.LocalDate

data class Item(
    val id: ID<Item>,
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val type: ItemType
) {
    constructor(
        id: ID<Item>,
        name: NonBlankString,
        sellByDate: LocalDate?,
        quality: Quality,
    ) : this(id, name, sellByDate, quality, type = typeFor(sellByDate, name))

    fun updatedBy(days: Int, on: LocalDate): Item {
        val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
        return dates.fold(this, type::update)
    }
}

data class PricedItem(
    val id: ID<Item>,
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>,
) {
    constructor(item: Item, price: Result4k<Price?, Exception>) :
        this(item.id, item.name, item.sellByDate, item.quality, price)
}

