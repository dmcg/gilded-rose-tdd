package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.domain.Item
import dev.forkhandles.result4k.*
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    private val stockFile: File,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item
) {
    fun stockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        stockFile.loadItems().flatMap { loaded ->
            val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
            when {
                daysOutOfDate > 0L -> loaded.updated(now, daysOutOfDate).savedTo(stockFile, now)
                else -> Success(loaded)
            }
        }

    private fun StockList.updated(
        now: Instant,
        daysOutOfDate: Long
    ): StockList = copy(
        lastModified = now,
        items = items.map { it.itemUpdate(daysOutOfDate.toInt(), LocalDate.ofInstant(now, zoneId)) }
    )
}

private fun StockList.savedTo(stockFile: File, now: Instant): Result<StockList, StockListLoadingError.IO> =
    try {
        val versionFile = File.createTempFile(
            "${stockFile.nameWithoutExtension}-$now-",
            "." + stockFile.extension,
            stockFile.parentFile
        )
        saveTo(versionFile)
        val tempFile = File.createTempFile("tmp", "." + stockFile.extension)
        saveTo(tempFile)
        if (!tempFile.renameTo(stockFile))
            error("Failed to rename temp to stockfile")
        Success(this)
    } catch (x: IOException) {
        Failure(StockListLoadingError.IO(x.message ?: "no message"))
    }

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()

