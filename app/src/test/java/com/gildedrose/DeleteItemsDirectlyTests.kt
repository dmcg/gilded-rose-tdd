package com.gildedrose

import com.gildedrose.domain.Item

class DeleteItemsDirectlyTests : DeleteItemsAcceptanceContract(
    delete= Fixture::deleteDirectly
)

private fun Fixture.deleteDirectly(toDelete: Set<Item>) {
    val ids = toDelete.map { it.id }.toSet()
    app.deleteItemsWithIds(ids)
}
