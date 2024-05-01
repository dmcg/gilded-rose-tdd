package com.gildedrose.domain

import dev.forkhandles.result4k.Result4k
import java.time.LocalDate

data class Item(
    val id: ID<Item>,
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Quality,
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
    }
}

data class PricedItem(
    val id: ID<Item>,
    val name: String,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>,
) {
    constructor(item: Item, price: Result4k<Price?, Exception>) :
        this(item.id, item.name, item.sellByDate, item.quality, price)

    init {
        require(name.isNotBlank()) { "Name must not be blank" }
    }
}

