package com.gildedrose.persistence

class InMemoryItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = InMemoryItems()
}
