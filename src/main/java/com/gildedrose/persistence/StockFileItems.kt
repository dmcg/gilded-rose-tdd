package com.gildedrose.persistence

import arrow.core.raise.Raise
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import java.io.File

class StockFileItems(private val stockFile: File) : Items<NoTX> {

    override fun <R> inTransaction(block: context(NoTX) () -> R) = block(NoTX)

    context(IO, NoTX, Raise<StockListLoadingError.IOError>) override fun save(
        stockList: StockList
    ): StockList {
        val versionFile = File.createTempFile(
            "${stockFile.nameWithoutExtension}-${stockList.lastModified}-",
            "." + stockFile.extension,
            stockFile.parentFile
        )
        stockList.saveTo(versionFile)
        val tempFile = File.createTempFile("tmp", "." + stockFile.extension)
        stockList.saveTo(tempFile)
        if (!tempFile.renameTo(stockFile))
            error("Failed to rename temp to stockfile")
        return stockList
    }

    context(IO, NoTX, Raise<StockListLoadingError>) override fun load(): StockList =
        stockFile.loadItems()
}

