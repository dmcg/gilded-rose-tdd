package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.foundation.IO

import com.gildedrose.foundation.runIO
import com.gildedrose.testing.IOResolver
import org.junit.jupiter.api.extension.ExtendWith

context(IO)
@ExtendWith(IOResolver::class)
class AddItemTests : AddItemAcceptanceContract(::addItemDirectly)

private fun addItemDirectly(app: App, newItem: Item) {
    runIO {
        app.addItem(newItem)
    }
}
