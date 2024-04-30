package com.gildedrose.domain

import dev.forkhandles.result4k.Result4k
import java.time.LocalDate

data class Item(
    val id: ID<Item>,
    val name: NonBlankString,
    val sellByDate: LocalDate?,
    val quality: Quality,
)

fun Item(
    id: ID<Item>,
    name: String,
    sellByDate: LocalDate?,
    quality: Quality,
) = Item(
    id,
    NonBlankString(name)!!,
    sellByDate,
    quality
)

data class PricedItem(
    val id: ID<Item>,
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>,
) {
    constructor(item: Item, price: Result4k<Price?, Exception>) :
        this(item.id, item.name, item.sellByDate, item.quality, price)
}

