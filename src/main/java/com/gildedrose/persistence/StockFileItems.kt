package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.theory.Action
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.io.File
import java.io.IOException

class StockFileItems(private val stockFile: File) : Items<Nothing?> {

    override fun <R> inTransaction(block: context(Nothing?) () -> R) = block(null)

    context(Nothing?) @Action
    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO> = try {
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
        Failure(StockListLoadingError.IO(x.message ?: "no message"))
    }

    context(Nothing?)
    @Action
    override fun load(): Result<StockList, StockListLoadingError> = stockFile.loadItems()
}

