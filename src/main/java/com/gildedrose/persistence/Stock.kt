package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.theory.Action
import com.gildedrose.theory.Calculation
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<Nothing?>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item
) {
    @Action
    fun stockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        items.inTransaction {
            items.load().flatMap { loaded ->
                val maybeUpdated = loaded.updatedIfOutOfDate(now)
                if (maybeUpdated == loaded)
                    Success(loaded)
                else
                    items.save(maybeUpdated)
            }
        }

    @Calculation
    private fun StockList.updatedIfOutOfDate(
        now: Instant
    ): StockList {
        val daysOutOfDate = lastModified.daysTo(now, zoneId)
        return when {
            daysOutOfDate > 0L -> {
                val updatedStockList = updated(
                    now,
                    daysOutOfDate.toInt(),
                    LocalDate.ofInstant(now, zoneId)
                )
                updatedStockList
            }

            else -> this
        }
    }

    @Calculation
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

