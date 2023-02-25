package com.gildedrose.persistence

class InMemoryItemsTests : ItemsContract<Nothing?>(
    items = InMemoryItems(),
    inTransaction = object : InTransaction<Nothing?> {
        override fun <R> invoke(block: context(Nothing?) () -> R): R = block(null)
    }
)
