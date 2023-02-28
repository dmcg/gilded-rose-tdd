package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Stock(
    val items: Items<Nothing?>,
    private val zoneId: ZoneId,
    private val itemUpdate: (Item).(days: Int, on: LocalDate) -> Item
) {
    fun stockList(now: Instant): Result4k<StockList, StockListLoadingError> {
        val thing: RequiresTransaction<Nothing?, StockList, StockListLoadingError> = items.loadToo().flatMap { loaded ->
            val daysOutOfDate = loaded.lastModified.daysTo(now, zoneId)
            when {
                daysOutOfDate > 0L -> {
                    val updatedStockList = loaded.updated(
                        now,
                        daysOutOfDate.toInt(),
                        LocalDate.ofInstant(now, zoneId)
                    )
                    items.saveToo(updatedStockList)
                }

                else -> {
                    RequiresTransaction<Nothing?, StockList, StockListLoadingError> { Success(loaded) }
                }
            }
        }
        return items.inTransaction {
            thing.run()
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

private fun <TX, R1, R2, E> RequiresTransaction<TX, R1, E>.flatMap(f: (R1) -> RequiresTransaction<TX, R2, E>) =
    RequiresTransaction<TX, R2, E> { tx ->
        val initialResult = this@flatMap.runWith(tx)
        when (initialResult) {
            is Failure<E> -> initialResult
            is Success<R1> -> {
                f(initialResult.value).runWith(tx)
            }
        }
}


internal fun Instant.daysTo(that: Instant, zone: ZoneId): Long =
    LocalDate.ofInstant(that, zone).toEpochDay() - LocalDate.ofInstant(this, zone).toEpochDay()

