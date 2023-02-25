package com.gildedrose.persistence

import java.nio.file.Files

class StockFileItemsTests : ItemsContract<Nothing?>(
    items = StockFileItems(
        stockFile = Files.createTempFile("stock", ".tsv").toFile()
    ),
    inTransaction = object : InTransaction<Nothing?> {
        override fun <R> invoke(block: context(Nothing?) () -> R): R = block(null)
    }
)
