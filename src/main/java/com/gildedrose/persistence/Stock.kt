package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<NoTX>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item
) {
    context(IO)
    fun stockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        items.inTransaction {
            items.load().flatMap { loaded ->
                val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
                when {
                    daysOutOfDate > 0L -> {
                        val updatedStockList = loaded.updated(
                            now,
                            daysOutOfDate.toInt(),
                            LocalDate.ofInstant(now, zoneId)
                        )
                        items.save(updatedStockList)
                    }

                    else -> Success(loaded)
                }
            }
        }

    private fun StockList.updated(
        now: Instant,
        daysOutOfDate: Int,
        localDate: LocalDate
    ): StockList = copy(
        lastModified = now,
        items = items.map { it.itemUpdate(daysOutOfDate, localDate) }
    )
}


internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()

