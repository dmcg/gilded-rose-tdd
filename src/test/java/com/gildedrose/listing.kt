package com.gildedrose

import java.time.Instant
import java.time.LocalDate

val oct29: LocalDate = LocalDate.parse("2021-10-29")

val someInstant: Instant = Instant.parse("2022-02-09T12:00:00Z")

val standardStockList = StockList(
    lastModified = someInstant,
    items = listOf(
        Item("banana", oct29.minusDays(1), 42u),
        Item("kumquat", oct29.plusDays(1), 101u)
    )
)
