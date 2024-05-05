package com.gildedrose.domain

import dev.forkhandles.result4k.Result4k
import java.time.LocalDate


typealias ItemID = String

data class ItemName(val value: String) : CharSequence by value {
    init {
        require(value.isNotBlank()) { "Name must not be blank" }
    }

}

data class Item(
    val id: ItemID,
    val _name: ItemName,
    val sellByDate: LocalDate?,
    val quality: Quality,
) {
    constructor(id: ItemID,
                name: String,
                sellByDate: LocalDate?,
                quality: Quality
    ) : this(id, ItemName(name), sellByDate, quality)
    init {
        require(id.isNotBlank()) { "id must not be blank" }
    }
    val name get() = _name.value
    fun copy(name: String): Item = this.copy(_name = ItemName(name))
}

data class PricedItem(
    val id: ItemID,
    val _name: ItemName,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>,
) {
    constructor(item: Item, price: Result4k<Price?, Exception>) :
        this(item.id, item._name, item.sellByDate, item.quality, price)

    val name get() = _name.value

    init {
        require(id.isNotBlank()) { "id must not be blank" }
    }
}

