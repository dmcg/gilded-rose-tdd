package com.gildedrose.pricing

import com.gildedrose.domain.*
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
        pricing.wrappedWith(retry(1, reporter = ::reportException))

    context(IO)
    fun load(now: Instant): Result<PricedStockList, StockListLoadingError> =
        loading(magic(), now).map {
            it.pricedBy(retryingPricing)
        }

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
        price = resultFrom {
            pricing(magic<IO>(), this)
        }.peekFailure(::reportException)
    )

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


