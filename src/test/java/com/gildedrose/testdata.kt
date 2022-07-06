package com.gildedrose

import com.gildedrose.domain.Item
import java.time.LocalDate


fun testItem(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = Item(name, sellByDate, quality) ?: error("Could not create an Item")

val oct29: LocalDate = LocalDate.parse("2021-10-29")
