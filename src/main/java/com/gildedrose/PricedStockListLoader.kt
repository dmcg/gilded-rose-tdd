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
    private val retryingPricing = retry(1, reporter = ::reportException) { it: Item ->
        runIO { pricing(magic(), it) }
    }
    private val threadPool = Executors.newFixedThreadPool(30)

    context(IO) fun load(now: Instant): StockLoadingResult =
        loading(magic(), now).map {
            it.pricedBy(retryingPricing)
        }

    private fun StockList.pricedBy(
        pricing: (Item) -> Price?
    ): StockList =
        runBlocking(threadPool.asCoroutineDispatcher()) {
            copy(items = items.parallelMapCoroutines { it.pricedBy(pricing) })
        }

    private fun Item.pricedBy(
        pricing: (Item) -> Price?
    ): Item =
        this.copy(
            price = resultFrom {
                pricing(this)
            }.peekFailure(::reportException)
        )

    private fun reportException(x: Exception) {
        analytics(UncaughtExceptionEvent(x))
    }
}


