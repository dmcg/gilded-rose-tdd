package com.gildedrose.persistence

import com.gildedrose.foundation.IO
import java.nio.file.Files

context(IO)
class StockFileItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = StockFileItems(
        Files.createTempFile("stock", ".tsv").toFile()
    )
}
