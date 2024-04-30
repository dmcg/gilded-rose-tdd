package com.gildedrose.persistence

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.persistence.StockListLoadingError.IOError
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import java.time.Instant

class DualItems(
    private val sourceOfTruth: Items<NoTX>,
    private val otherItems: DbItems,
    private val analytics: Analytics
) : Items<DbTxContext> {

    override fun <R> inTransaction(block: context(DbTxContext) () -> R): R =
        otherItems.inTransaction(block)

    context(DbTxContext)
    override fun save(stockList: StockList): Result<StockList, IOError> =
        sourceOfTruth.inTransaction {
            sourceOfTruth.save(stockList)
        }.also { result ->
            try {
                val otherResult = otherItems.save(stockList)
                if (result != otherResult)
                    analytics(stocklistSavingMismatch(result, otherResult))
            } catch (throwable: Throwable) {
                analytics(StockListSavingExceptionCaught(throwable))
            }
        }

    context(DbTxContext)
    override fun load(): Result<StockList, StockListLoadingError> =
        sourceOfTruth.inTransaction {
            sourceOfTruth.load()
        }.also { result ->
            try {
                val otherResult = otherItems.load()
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
    result: Result<StockList, IOError>,
    otherResult: Result<StockList, IOError>
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
