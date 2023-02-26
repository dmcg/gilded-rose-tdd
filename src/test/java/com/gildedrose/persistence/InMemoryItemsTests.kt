package com.gildedrose.persistence

class InMemoryItemsTests : ItemsContract<Nothing?>(
    items = InMemoryItems(),
    inTransaction = { block -> block(null) }
)
