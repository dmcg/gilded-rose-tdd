package com.gildedrose.pricing

import com.gildedrose.domain.*
import com.gildedrose.foundation.*
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peekFailure
import dev.forkhandles.result4k.resultFrom
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.concurrent.Executors

typealias StockLoadingResult = Result<StockList, StockListLoadingError>

class PricedStockListLoader(
    private val loading: context(TXContext) (Instant) -> StockLoadingResult,
    pricing: (Item) -> Price?,
    private val analytics: Analytics
) {
    private val threadPool = Executors.newFixedThreadPool(30)
    private val retryingPricing: (Item) -> Price? =
        pricing.wrappedWith(retry(1, reporter = ::reportException))

    context(TXContext)
    fun load(now: Instant): Result<PricedStockList, StockListLoadingError> =
        loading(magic(),now).map {
            it.pricedBy(retryingPricing)
        }

    private fun StockList.pricedBy(
        pricing: (Item) -> Price?
    ): PricedStockList =
        runBlocking(threadPool.asCoroutineDispatcher()) {
            PricedStockList(
                lastModified = lastModified,
                items = items.parallelMapCoroutines { it.pricedBy(pricing) }
            )
        }

    private fun Item.pricedBy(
        pricing: (Item) -> Price?
    ) = PricedItem(
        this,
        price = resultFrom {
            pricing(this)
        }.peekFailure(::reportException)
    )

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


