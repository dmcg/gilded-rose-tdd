package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<Unit>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item
) {
    fun stockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        items.withTransaction { tx ->
            items.load(tx).flatMap { loaded ->
                val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
                when {
                    daysOutOfDate > 0L -> {
                        val updatedStockList = loaded.updated(
                            now,
                            daysOutOfDate.toInt(),
                            LocalDate.ofInstant(now, zoneId)
                        )
                        items.save(updatedStockList, tx)
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

