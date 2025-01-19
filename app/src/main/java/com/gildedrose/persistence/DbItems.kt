package com.gildedrose.persistence

import com.gildedrose.db.tables.Items.ITEMS
import com.gildedrose.domain.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.Record5
import org.jooq.impl.DSL
import org.jooq.impl.DSL.max
import java.time.Instant
import java.time.LocalDate

class DbTxContext(val dslContext: DSLContext) : TXContext()

class DbItems(
    dslContext: DSLContext,
) : Items<DbTxContext> {

    private val forInTransaction = object {
        @Suppress("UnnecessaryVariable")
        val untransactionalDSLContext = dslContext
    }

    override fun <R> inTransaction(block: context(DbTxContext) () -> R): R =
        forInTransaction.untransactionalDSLContext.transactionResult { trx: Configuration ->
            val txContext = DbTxContext(trx.dsl())
            block(txContext)
        }

    context(DbTxContext)
    override fun save(
        stockList: StockList,
    ): Result<StockList, StockListLoadingError.IOError> {
        val toSave = when {
            stockList.items.isEmpty() -> listOf(sentinelItem)
            else -> stockList.items
        }
        toSave.forEach<Item> { item ->
            with(ITEMS) {
                dslContext.insertInto(ITEMS)
                    .set(ID, item.id.toString())
                    .set(MODIFIED, stockList.lastModified)
                    .set(NAME, item.name.toString())
                    .set(QUALITY, item.quality.valueInt)
                    .set(SELLBYDATE, item.sellByDate)
                    .execute()
            }
        }
        return Success(stockList)
    }

    context(DbTxContext)
    override fun load(): Result<StockList, StockListLoadingError> {
        val stockList = with(ITEMS) {
            val records = dslContext.select(
                ID,
                MODIFIED,
                NAME,
                QUALITY,
                SELLBYDATE
            ).from(ITEMS)
                .where(
                    MODIFIED.eq(DSL.select(max(MODIFIED)).from(ITEMS))
                ).fetch()
            if (records.isEmpty())
                StockList(Instant.EPOCH, emptyList())
            else {
                val lastModified: Instant = records.first()[MODIFIED]
                val items: List<Item> = records.map<Item> { it.toItem() }
                val isEmpty = (items.singleOrNull() == sentinelItem)
                StockList(
                    lastModified,
                    if (isEmpty) emptyList() else items
                )
            }
        }
        return Success(stockList)
    }
}

private val sentinelItem = Item(
    id = ID("NO-ITEMS-SAVED")!!,
    name = NonBlankString("THIS IS NOT AN ITEM")!!,
    sellByDate = null,
    quality = Quality(Int.MAX_VALUE)!!
)

private fun Record5<String, Instant, String, Int, LocalDate>.toItem() =
    Item(
        id = ID(this[ITEMS.ID]) ?: error("Invalid ID"),
        name = NonBlankString(this[ITEMS.NAME]) ?: error("Invalid name"),
        sellByDate = this[ITEMS.SELLBYDATE],
        quality = Quality(this[ITEMS.QUALITY]) ?: error("Invalid quality")
    )

