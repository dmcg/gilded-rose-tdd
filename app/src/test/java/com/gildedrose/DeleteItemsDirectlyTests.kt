package com.gildedrose

import com.gildedrose.domain.Item

class DeleteItemsDirectlyTests : DeleteItemsAcceptanceContract(
    doDelete = ::deleteDirectly
)

private fun deleteDirectly(app: App<*>, toDelete: Set<Item>) {
    val ids = toDelete.map { it.id }.toSet()
    app.deleteItemsWithIds(ids)
}
