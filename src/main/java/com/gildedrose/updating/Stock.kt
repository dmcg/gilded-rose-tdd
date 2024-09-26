package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.magic
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock<TX>(
    val items: Items<TX>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item = Item::updatedBy
) {
    context(TX)
    fun loadAndUpdateStockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        items.load(magic()).flatMap { loadedStockList ->
            val daysOutOfDate = loadedStockList.lastModified.daysTo(now, zoneId)
            when {
                daysOutOfDate > 0L -> {
                    val updatedStockList = loadedStockList.updated(
                        now,
                        daysOutOfDate.toInt(),
                        LocalDate.ofInstant(now, zoneId)
                    )
                    items.save(updatedStockList, magic())
                }

                else -> Success(loadedStockList)
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

fun Item.updatedBy(days: Int, on: LocalDate): Item {
    val type = typeFor(sellByDate, name)
    val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
    return dates.fold(this, type::update)
}

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()
