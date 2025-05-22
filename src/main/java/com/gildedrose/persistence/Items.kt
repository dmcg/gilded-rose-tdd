@file:Suppress("CONTEXT_RECEIVERS_DEPRECATED")

package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import dev.forkhandles.result4k.Result

/**
 * The transaction that Items methods are running in.
 */
open class TXContext

/**
 * For use when an Items implementation doesn't support transactions.
 */
object NoTX: TXContext()

/**
 * Repository for our StockList.
 */
interface Items<out TX: TXContext> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError>

    context(TX) fun load(): Result<StockList, StockListLoadingError>
}
