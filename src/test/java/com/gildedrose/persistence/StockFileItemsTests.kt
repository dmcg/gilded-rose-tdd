package com.gildedrose.persistence

import java.nio.file.Files

class StockFileItemsTests : ItemsContract<Nothing?>(
    items = StockFileItems(Files.createTempFile("stock", ".tsv").toFile()),
    inTransaction = { block -> block(null) }
)
