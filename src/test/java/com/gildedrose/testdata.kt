package com.gildedrose

import com.gildedrose.domain.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.time.LocalDate


fun item(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = item(initialsFrom(name) + "1", name, sellByDate, quality)

fun item(
    id: String,
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = Item(
    ID(id)!!,
    NonBlankString(name)!!,
    sellByDate,
    Quality(quality)!!
)

fun initialsFrom(name: String) = name.split(" ").map { it[0] }.joinToString("").uppercase()

fun Item.withNoPrice() = this.copy(price = null)
fun Item.withPriceResult(price: Price?) = this.withPriceResult(Success(price))
fun Item.withPriceResult(price: Result<Price?, Exception>) = this.copy(price = price)

fun StockList.withItems(vararg items: Item) = this.copy(items = items.toList())
fun StockList.withoutPrices() =
    this.copy(items = items.map { item -> item.withNoPrice() })


val oct29: LocalDate = LocalDate.parse("2021-10-29")
