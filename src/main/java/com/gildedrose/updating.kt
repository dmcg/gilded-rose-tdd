package com.gildedrose

fun updateItems(items: List<Item>, days: Int) = items.map {
    it.updatedBy(days)
}
