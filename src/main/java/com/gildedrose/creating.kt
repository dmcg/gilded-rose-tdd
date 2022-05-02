package com.gildedrose

import java.time.LocalDate

fun itemOf(
    name: String,
    sellByDate: LocalDate?,
    quality: Int,
) = Item(name, sellByDate, quality, updaterFor(sellByDate, name))
