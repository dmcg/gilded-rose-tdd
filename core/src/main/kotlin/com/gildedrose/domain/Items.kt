package com.gildedrose.domain

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
interface Items<TX> {

    fun <R> inTransaction(block: context(TX) () -> R): R

    context(_: TX) fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError>

    context(_: TX) fun load(): Result<StockList, StockListLoadingError>
}
