package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<TXContext>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item = Item::updatedBy
) {
    context(TXContext)
    fun loadAndUpdateStockList(now: Instant): Result4k<StockList, StockListLoadingError> =
        items.load().flatMap { loadedStockList ->
            val daysOutOfDate = loadedStockList.lastModified.daysTo(now, zoneId)
            when {
                daysOutOfDate > 0L -> {
                    val updatedStockList = loadedStockList.updated(
                        now,
                        daysOutOfDate.toInt(),
                        LocalDate.ofInstant(now, zoneId)
                    )
                    items.save(updatedStockList)
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
    val type = typeFor(sellByDate, _name.value)
    val dates = (1 - days).rangeTo(0).map { on.plusDays(it.toLong()) }
    return dates.fold(this, type::update)
}

internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()
