@file:Suppress("CONTEXT_RECEIVERS_DEPRECATED")

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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias StockLoadingResult = Result<StockList, StockListLoadingError>

class PricedStockListLoader(
    private val loading: context(TXContext) (Instant) -> StockLoadingResult,
    pricing: (Item) -> Price?,
    private val analytics: Analytics,
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(30)
) {
    private val retryingPricing: (Item) -> Price? =
        pricing.wrappedWith(retry(1, reporter = ::reportException))

    context(TXContext)
    fun load(now: Instant): Result<PricedStockList, StockListLoadingError> =
        loading(magic(), now).map {
            val pricedItems = runBlocking(threadPool.asCoroutineDispatcher()) {
                it.items.parallelMapCoroutines { item ->
                    PricedItem(
                        item,
                        price = resultFrom { retryingPricing(item) }
                            .peekFailure(::reportException)
                    )
                }
            }
            PricedStockList(it.lastModified, pricedItems)
        }

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


