package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.NonNegativeInt
import java.time.LocalDate


fun testItem(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
): Item = Item(NonBlankString(name)!!, sellByDate, NonNegativeInt(quality)!!)

val oct29: LocalDate = LocalDate.parse("2021-10-29")
