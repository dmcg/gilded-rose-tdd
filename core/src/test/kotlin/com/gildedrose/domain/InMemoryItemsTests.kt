package com.gildedrose.domain

import com.gildedrose.testing.InMemoryItems

class InMemoryItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = InMemoryItems()
}
