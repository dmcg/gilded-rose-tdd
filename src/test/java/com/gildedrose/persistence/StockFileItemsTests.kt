package com.gildedrose.persistence

import java.nio.file.Files

class StockFileItemsTests : ItemsContract<NoTX>() {
    override val items: Items<NoTX> = StockFileItems(
        Files.createTempFile("stock", ".tsv").toFile()
    )
}
