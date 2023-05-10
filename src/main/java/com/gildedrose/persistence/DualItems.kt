package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.jooq.JooqItems
import com.gildedrose.persistence.jooq.JooqTXContext
import dev.forkhandles.result4k.Result

class DualItems(
    private val sourceOfTruth: Items<NoTX>,
    private val otherItems: JooqItems,
    private val analytics: Analytics
) : Items<JooqTXContext> {

    override fun <R> inTransaction(
        block: context(JooqTXContext) () -> R
    ): R = otherItems.inTransaction(block)

    context(IO, JooqTXContext)
    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError> {
        TODO("Not yet implemented")
    }

    context(IO, JooqTXContext)
    override fun load(): Result<StockList, StockListLoadingError> {
        val result = sourceOfTruth.inTransaction { sourceOfTruth.load() }
        val otherItemsResult = otherItems.load()
        if (result != otherItemsResult)
            analytics(StocklistMismatch(result, otherItemsResult))
        return result
    }

}

data class StocklistMismatch(
    val expected: Result<StockList, StockListLoadingError>,
    val actual: Result<StockList, StockListLoadingError>,
) : AnalyticsEvent {

}
