package com.gildedrose.persistence.inMemory

import com.gildedrose.foundation.IO
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX

context(IO)
class InMemoryItemsTests : ItemsContract<NoTX> {
    override val items: Items<NoTX> = InMemoryItems()
}
