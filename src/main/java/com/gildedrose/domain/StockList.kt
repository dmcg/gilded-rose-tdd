package com.gildedrose.domain

import com.gildedrose.theory.Data
import java.time.Instant

@Data
data class StockList(
    val lastModified: Instant,
    val items: List<Item>
) : List<Item> by items
