package com.gildedrose

import com.gildedrose.domain.Item

class DirectActor : Actor() {
    override fun delete(fixture: Fixture, items: Set<Item>) {
        val ids = items.map { it.id }.toSet()
        fixture.app.deleteItemsWithIds(ids)
    }

    override fun add(fixture: Fixture, item: Item) {
        fixture.app.addItem(item)
    }
}
