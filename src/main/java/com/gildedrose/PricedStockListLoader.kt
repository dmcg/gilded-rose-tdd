package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import com.gildedrose.persistence.StockListLoadingError
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
    private val loading: context(IO) (Instant) -> StockLoadingResult,
    pricing: context(IO) (Item) -> Price?,
    private val analytics: Analytics
) {
    private val threadPool = Executors.newFixedThreadPool(30)
    private val retryingPricing: context(IO) (Item) -> Price? =
        pricing.transformedBy { f ->
            retry(1, reporter = ::reportException, f)
        }

    context(IO)
    fun load(now: Instant): StockLoadingResult =
        loading(magic(), now).map {
            it.pricedBy(retryingPricing)
        }

    context(IO)
    private fun StockList.pricedBy(
        pricing: context(IO) (Item) -> Price?
    ): StockList =
        runBlocking(threadPool.asCoroutineDispatcher()) {
            copy(items = items.parallelMapCoroutines { it.pricedBy(pricing) })
        }

    context(IO)
    private fun Item.pricedBy(
        pricing: context(IO) (Item) -> Price?
    ): Item =
        this.copy(
            price = resultFrom {
                pricing(magic<IO>(), this)
            }.peekFailure(::reportException)
        )

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


