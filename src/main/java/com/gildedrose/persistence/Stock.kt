package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.domain.Item
import dev.forkhandles.result4k.*
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    private val stockFile: File,
    private val zoneId: ZoneId,
    private val update: (items: List<Item>, days: Int, on: LocalDate) -> List<Item>
) {
    fun stockList(now: Instant): Result4k<StockList, Nothing?> {
        val loaded = stockFile.loadItems().recover { return Failure(null) }
        val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
        val potentiallyUpdatedStockList = when {
            daysOutOfDate > 0L -> loaded.copy(
                lastModified = now,
                update(loaded.items, daysOutOfDate.toInt(), LocalDate.ofInstant(now, zoneId))
            )
            else -> loaded
        }
        if (potentiallyUpdatedStockList.lastModified != loaded.lastModified)
            save(potentiallyUpdatedStockList, now)
        return Success(potentiallyUpdatedStockList)
    }

    private fun save(stockList: StockList, now: Instant) {
        val versionFile = File.createTempFile(
            "${stockFile.nameWithoutExtension}-$now-",
            "." + stockFile.extension,
            stockFile.parentFile
        )
        stockList.saveTo(versionFile)
        val tempFile = File.createTempFile("tmp", "." + stockFile.extension)
        stockList.saveTo(tempFile)
        if (!tempFile.renameTo(stockFile))
            error("Failed to rename temp to stockfile")
    }
}

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()

