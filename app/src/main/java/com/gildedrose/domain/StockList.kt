package com.gildedrose.domain

import java.time.Instant

data class StockList(
    val lastModified: Instant,
    val items: List<Item>
) : List<Item> by items

data class PricedStockList(
    val lastModified: Instant,
    val items: List<PricedItem>
) : List<PricedItem> by items
