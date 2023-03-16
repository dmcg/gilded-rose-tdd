package com.gildedrose.persistence

class InMemoryItemsTests : ItemsContract<NoTX>(
    items = InMemoryItems()
)
