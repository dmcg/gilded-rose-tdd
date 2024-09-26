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
interface Items<TX> {

    fun <R> inTransaction(block: (TX) -> R): R

    fun save(
        stockList: StockList,
        tx: TX
    ): Result<StockList, StockListLoadingError.IOError>

    fun load(tx: TX): Result<StockList, StockListLoadingError>
}
