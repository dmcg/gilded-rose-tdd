package com.gildedrose.pricing

import arrow.core.raise.Raise
import com.gildedrose.domain.*
import com.gildedrose.foundation.*
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import dev.forkhandles.result4k.peekFailure
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.concurrent.Executors

class PricedStockListLoader(
    private val loading: context(IO, TXContext, Raise<StockListLoadingError>) (Instant) -> StockList,
    pricing: context(IO) (Item) -> Price?,
    private val analytics: Analytics
) {
    private val threadPool = Executors.newFixedThreadPool(30)
    private val retryingPricing: context(IO) (Item) -> Price? =
        pricing.wrappedWith(retry(1, reporter = ::reportException))

    context(IO, TXContext, Raise<StockListLoadingError>)
    fun load(now: Instant): PricedStockList = loading(now).pricedBy(retryingPricing)

    context(IO)
    private fun StockList.pricedBy(
        pricing: context(IO) (Item) -> Price?
    ): PricedStockList =
        runBlocking(threadPool.asCoroutineDispatcher()) {
            PricedStockList(
                lastModified = lastModified,
                items = items.parallelMapCoroutines { it.pricedBy(pricing) }
            )
        }

    context(IO)
    private fun Item.pricedBy(
        pricing: context(IO) (Item) -> Price?
    ) = PricedItem(
        this,
        price = resultCatch {
            pricing(this)
        }.peekFailure(::reportException)
    )

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


