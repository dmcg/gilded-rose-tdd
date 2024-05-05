package com.gildedrose.domain

import dev.forkhandles.result4k.Result4k
import java.time.LocalDate


typealias ItemID = String

@JvmInline
value class ItemName private constructor(val value: String) : CharSequence by value {
    init {
        require(value.isNotBlank()) { "Name must not be blank" }
    }
    companion object {
        operator fun invoke(value: String): ItemName? = if (value.isBlank()) null else ItemName(value)
    }
}

data class Item(
    val id: ItemID,
    val name: ItemName,
    val sellByDate: LocalDate?,
    val quality: Quality,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
    }
}

data class PricedItem(
    val id: ItemID,
    val name: ItemName,
    val sellByDate: LocalDate?,
    val quality: Quality,
    val price: Result4k<Price?, Exception>,
) {
    constructor(item: Item, price: Result4k<Price?, Exception>) :
        this(item.id, item.name, item.sellByDate, item.quality, price)

    init {
        require(id.isNotBlank()) { "id must not be blank" }
    }
}

