package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.io.File
import java.io.IOException

class StockFileItems(private val stockFile: File) : Items<NoTX> {

    override fun <R> inTransaction(block: (NoTX) -> R): R = block(NoTX)

    override fun save(
        stockList: StockList,
        tx: NoTX
    ): Result<StockList, StockListLoadingError.IOError> = try {
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
        Success(stockList)
    } catch (x: IOException) {
        Failure(StockListLoadingError.IOError(x.message ?: "no message"))
    }

    override fun load(tx: NoTX): Result<StockList, StockListLoadingError> {
        return stockFile.loadItems()
    }
}

