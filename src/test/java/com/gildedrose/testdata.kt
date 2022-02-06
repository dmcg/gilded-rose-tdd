package com.gildedrose

import java.time.Instant
import java.time.LocalDate

val oct29: LocalDate = LocalDate.parse("2021-10-29")
val standardStockList = StockList(
    Instant.now(),
    listOf(
        Item("banana", oct29.minusDays(1), 42u),
        Item("kumquat", oct29.plusDays(1), 101u)
    )
)
