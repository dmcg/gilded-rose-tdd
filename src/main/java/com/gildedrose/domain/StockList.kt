package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import java.time.Instant

@Calculation
data class StockList(
    val lastModified: Instant,
    val items: List<Item>
) : List<Item> by items
