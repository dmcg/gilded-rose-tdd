package com.gildedrose.persistence.inMemory

import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX

class InMemoryItemsTests : ItemsContract<NoTX>(
    items = InMemoryItems()
)
