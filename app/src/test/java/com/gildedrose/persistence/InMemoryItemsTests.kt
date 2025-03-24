package com.gildedrose.persistence

import com.gildedrose.domain.Items
import com.gildedrose.domain.NoTX

class InMemoryItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = InMemoryItems()
}
