package com.gildedrose.persistence.inMemory

import com.gildedrose.foundation.IO
import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX

context(IO)
class InMemoryItemsTests : ItemsContract<NoTX>(
    items = InMemoryItems()
)
