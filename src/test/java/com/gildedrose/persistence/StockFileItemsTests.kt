package com.gildedrose.persistence

import java.nio.file.Files

class StockFileItemsTests : ItemsContract<Unit>(
    StockFileItems(
        Files.createTempFile("stock", ".tsv").toFile()
    )
)
