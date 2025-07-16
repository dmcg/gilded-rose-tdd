package com.gildedrose

import com.gildedrose.domain.Item

/**
 * Actor implementation that interacts with the system directly.
 */
class DirectActor : Actor() {
    /**
     * Adds an item to the system by calling the app's addItem method directly.
     */
    override fun adds(fixture: Fixture, item: Item) {
        fixture.app.addItem(item)
    }

    /**
     * Deletes items from the system by calling the app's deleteItemsWithIds method directly.
     */
    override fun deletes(fixture: Fixture, items: Set<Item>) {
        val ids = items.map { it.id }.toSet()
        fixture.app.deleteItemsWithIds(ids)
    }
}
