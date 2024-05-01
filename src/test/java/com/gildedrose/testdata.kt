package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.Quality
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
    id,
    name,
    sellByDate,
    Quality(quality)!!
)

fun initialsFrom(name: String) = name.split(" ").map { it[0] }.joinToString("").uppercase()

fun PricedItem.withNoPrice() = Item(id, name, sellByDate, quality)
fun Item.withPriceResult(price: Price?) = this.withPriceResult(Success(price))
fun Item.withPriceResult(price: Result<Price?, Exception>) = PricedItem(this, price = price)

val oct29: LocalDate = LocalDate.parse("2021-10-29")
