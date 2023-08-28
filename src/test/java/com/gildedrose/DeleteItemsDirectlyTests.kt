package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.runIO
import com.gildedrose.testing.IOResolver
import org.junit.jupiter.api.extension.ExtendWith

context(IO)
@ExtendWith(IOResolver::class)
class DeleteItemsDirectlyTests : DeleteItemsAcceptanceContract(
    doDelete = ::deleteDirectly
)

private fun deleteDirectly(app: App, toDelete: Set<Item>) {
    val ids = toDelete.map { it.id }.toSet()
    runIO { app.deleteItemsWithIds(ids) }
}
