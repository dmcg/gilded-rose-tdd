package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.magic
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

    fun <R> inTransactionToo(block: (TX) -> R): R = inTransaction { block(magic()) }

    fun save(
        stockList: StockList,
        tx: TX
    ): Result<StockList, StockListLoadingError.IOError>

    context(TX) fun load(): Result<StockList, StockListLoadingError>
}
