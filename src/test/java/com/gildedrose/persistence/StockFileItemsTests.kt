package com.gildedrose.persistence

import java.nio.file.Files

class StockFileItemsTests : ItemsContract<NoTX>(
    items = StockFileItems(Files.createTempFile("stock", ".tsv").toFile())
)
