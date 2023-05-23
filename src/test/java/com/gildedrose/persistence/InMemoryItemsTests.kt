package com.gildedrose.persistence

import com.gildedrose.foundation.IO

context(IO)
class InMemoryItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = InMemoryItems()
}
