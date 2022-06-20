package com.gildedrose

import com.gildedrose.domain.Item
import java.time.Instant

data class StockList(
    val lastModified: Instant,
    val items: List<Item>
) : List<Item> by items
