package com.gildedrose.persistence.stockFile

import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX
import java.nio.file.Files

class StockFileItemsTests : ItemsContract<NoTX>(
    items = StockFileItems(Files.createTempFile("stock", ".tsv").toFile())
)
