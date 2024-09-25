package com.gildedrose

import com.gildedrose.domain.Item

class AddItemTests : AddItemAcceptanceContract(::addItemDirectly)

private fun addItemDirectly(app: App, newItem: Item) {
    app.addItem(newItem)
}
