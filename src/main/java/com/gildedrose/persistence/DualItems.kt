package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.AnalyticsEvent
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import java.time.Instant

class DualItems(
    private val sourceOfTruth: Items<NoTX>,
    private val otherItems: DbItems,
    private val analytics: Analytics
) : Items<DbTxContext> {

    override fun <R> inTransaction(block: (DbTxContext) -> R): R = otherItems.inTransaction(block)

    override fun save(
        stockList: StockList,
        tx: DbTxContext
    ): Result<StockList, StockListLoadingError.IOError> = sourceOfTruth.inTransaction { innerTx ->
        sourceOfTruth.save(stockList, innerTx)
    }.also { result ->
        try {
            val otherResult = with(tx) { otherItems.save(stockList, tx) }
            if (result != otherResult)
                analytics(stocklistSavingMismatch(result, otherResult))
        } catch (throwable: Throwable) {
            analytics(StockListSavingExceptionCaught(throwable))
        }
    }

    override fun load(tx: DbTxContext): Result<StockList, StockListLoadingError> =
        sourceOfTruth.inTransaction {
            sourceOfTruth.load(it)
        }.also { result ->
            try {
                val otherResult = otherItems.load(tx)
                if (result != otherResult)
                    analytics(stocklistLoadingMismatch(result, otherResult))
            } catch (throwable: Throwable) {
                analytics(StockListLoadingExceptionCaught(throwable))
            }
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

data class StockListLoadingExceptionCaught(
    val message: String,
    val stackTrace: List<String>
) : AnalyticsEvent {
    constructor(exception: Throwable) : this(
        exception.message.orEmpty(),
        exception.stackTrace.map(StackTraceElement::toString)
    )
}

data class StockListSavingExceptionCaught(
    val message: String,
    val stackTrace: List<String>
) : AnalyticsEvent {
    constructor(exception: Throwable) : this(
        exception.message.orEmpty(),
        exception.stackTrace.map(StackTraceElement::toString)
    )
}
