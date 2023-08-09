package com.gildedrose.persistence

import arrow.core.raise.Raise
import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import java.time.Instant

class DualItems(
    private val sourceOfTruth: Items<NoTX>,
    private val otherItems: DbItems,
    private val analytics: Analytics
) : Items<DbTxContext> {

    override fun <R> inTransaction(
        block: context(DbTxContext) () -> R
    ): R = otherItems.inTransaction(block)

    context(IO, DbTxContext, Raise<StockListLoadingError.IOError>)
    override fun save(
        stockList: StockList
    ): StockList {
        val result = result4k { sourceOfTruth.inTransaction { sourceOfTruth.save(stockList) } }
        val otherResult = result4k { otherItems.save(stockList) }
        if (result != otherResult)
            analytics(stocklistSavingMismatch(result, otherResult))
        return result.bind()
    }

    context(IO, DbTxContext, Raise<StockListLoadingError>)
    override fun load(): StockList {
        val result = result4k { sourceOfTruth.inTransaction { sourceOfTruth.load() } }
        val otherResult = result4k { otherItems.load() }
        if (result != otherResult)
            analytics(stocklistLoadingMismatch(result, otherResult))
        return result.bind()
    }
}

private fun Result<StockList, StockListLoadingError>.toRenderable():
    Result<JacksonRenderableStockList, StockListLoadingError> =
    this.map { stockList -> JacksonRenderableStockList(stockList.lastModified, stockList.items) }

internal fun stocklistSavingMismatch(
    result: Result<StockList, StockListLoadingError.IOError>,
    otherResult: Result<StockList, StockListLoadingError.IOError>
) = StocklistSavingMismatch(result.toRenderable(), otherResult.toRenderable())

data class StocklistLoadingMismatch(
    val expected: Result<JacksonRenderableStockList, StockListLoadingError>,
    val actual: Result<JacksonRenderableStockList, StockListLoadingError>,
) : AnalyticsEvent

internal fun stocklistLoadingMismatch(
    result: Result<StockList, StockListLoadingError>,
    otherResult: Result<StockList, StockListLoadingError>
) = StocklistLoadingMismatch(result.toRenderable(), otherResult.toRenderable())

data class StocklistSavingMismatch(
    val expected: Result<JacksonRenderableStockList, StockListLoadingError>,
    val actual: Result<JacksonRenderableStockList, StockListLoadingError>,
) : AnalyticsEvent

data class JacksonRenderableStockList(
    val lastModified: Instant,
    val items: List<Item>
)
