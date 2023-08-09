package com.gildedrose.persistence

import arrow.core.raise.Raise
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO

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

    context(IO, TX, Raise<StockListLoadingError.IOError>)
    fun save(stockList: StockList): StockList

    context(IO, TX, Raise<StockListLoadingError>)
    fun load(): StockList
}
