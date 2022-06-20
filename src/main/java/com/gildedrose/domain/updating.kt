package com.gildedrose.domain

import java.time.LocalDate

fun updateItems(items: List<Item>, days: Int, on: LocalDate) = items.map {
    it.updatedBy(days, on)
}

