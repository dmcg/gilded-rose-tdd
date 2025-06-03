package com.gildedrose.updating

import com.gildedrose.domain.Item
import com.gildedrose.domain.Items
import com.gildedrose.domain.StockList
import com.gildedrose.domain.StockListLoadingError
import com.gildedrose.updating.StockUpdateDecision.DoNothing
import com.gildedrose.updating.StockUpdateDecision.SaveUpdate
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.flatMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock<TX>(
    val items: Items<TX>,
    private val zoneId: ZoneId,
) {
    context(TX)
    fun loadAndUpdateStockList(
        now: Instant,
    ): Result4k<StockList, StockListLoadingError> =
        items.load().flatMap { loadedStockList ->
            val decision = mayBeUpdate(loadedStockList, now, zoneId)
            when (decision) {
                is SaveUpdate -> items.save(decision.updatedStockList)
                is DoNothing -> decision.loadedStockList.asSuccess()
            }
        }
}

internal sealed class StockUpdateDecision {
    data class SaveUpdate(val updatedStockList: StockList) : StockUpdateDecision()
    data class DoNothing(val loadedStockList: StockList) : StockUpdateDecision()
}

internal fun mayBeUpdate(
    loadedStockList: StockList,
    now: Instant,
    zoneId: ZoneId,
): StockUpdateDecision {
    val daysOutOfDate = loadedStockList
        .lastModified.daysTo(now, zoneId).toInt()
    return when {
        daysOutOfDate > 0 -> SaveUpdate(
            StockList(
                now,
                loadedStockList.items.map {
                    it.updatedBy(
                        daysOutOfDate,
                        LocalDate.ofInstant(now, zoneId)
                    )
                })
        )

        else -> DoNothing(loadedStockList)
    }
}

fun Item.updatedBy(days: Int, on: LocalDate): Item {
    val type = typeFor(sellByDate, name)
    val dates: List<LocalDate> = (1 - days).rangeTo(0).map {
        on.plusDays(it.toLong())
    }
    return dates.fold(this, type::update)
}

internal fun Instant.daysTo(
    that: Instant,
    zone: ZoneId,
): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() -
        LocalDate.ofInstant(this, zone).toEpochDay()
