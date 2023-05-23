package com.gildedrose.persistence.stockFile

import com.gildedrose.foundation.IO
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX
import java.nio.file.Files

context(IO)
class StockFileItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = StockFileItems(
        Files.createTempFile("stock", ".tsv").toFile()
    )
}
