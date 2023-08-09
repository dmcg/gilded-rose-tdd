package com.gildedrose.updating

import arrow.core.raise.Raise
import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<TXContext>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item = Item::updatedBy
) {
    context(IO, TXContext, Raise<StockListLoadingError>)
    fun loadAndUpdateStockList(now: Instant): StockList {
        val loadedStockList = items.load()
        val daysOutOfDate = loadedStockList.lastModified.daysTo(now, zoneId)
        return when {
            daysOutOfDate > 0L -> {
                loadedStockList.updated(
                    now,
                    daysOutOfDate.toInt(),
                    LocalDate.ofInstant(now, zoneId)
                ).also { items.save(it) }
            }

            else -> loadedStockList
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
