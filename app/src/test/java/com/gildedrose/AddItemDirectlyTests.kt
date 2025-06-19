package com.gildedrose

import com.gildedrose.domain.Item

class AddItemTests : AddItemAcceptanceContract(Fixture::addItemDirectly)

private fun Fixture.addItemDirectly(newItem: Item) {
    app.addItem(newItem)
}
