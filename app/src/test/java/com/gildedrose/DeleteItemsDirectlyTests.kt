package com.gildedrose

import com.gildedrose.domain.Item

class DeleteItemsDirectlyTests : DeleteItemsAcceptanceContract(
    alison = DirectActor()
)

class DirectActor : Actor() {
    override fun delete(fixture: Fixture, items: Set<Item>) {
        val ids = items.map { it.id }.toSet()
        fixture.app.deleteItemsWithIds(ids)
    }
}

