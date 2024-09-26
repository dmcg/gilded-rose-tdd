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

open class DbItems(
    dslContext: DSLContext
) : Items<DbTxContext> {

    private val forInTransaction = object {
        @Suppress("UnnecessaryVariable")
        val untransactionalDSLContext = dslContext
    }

    override fun <R> inTransaction(block: (DbTxContext) -> R): R =
        forInTransaction.untransactionalDSLContext.transactionResult { trx: Configuration ->
            val txContext = DbTxContext(trx.dsl())
            block(txContext)
        }

    override fun save(
        stockList: StockList,
        tx: DbTxContext
    ): Result<StockList, StockListLoadingError.IOError> {
        tx.dslContext.save(stockList)
        return Success(stockList)
    }

    override fun load(tx: DbTxContext): Result<StockList, StockListLoadingError> {
        return Success(tx.dslContext.load())
    }
}

private val sentinelItem = Item(
    id = ID("NO-ITEMS-SAVED")!!,
    name = NonBlankString("THIS IS NOT AN ITEM")!!,
    sellByDate = null,
    quality = Quality(Int.MAX_VALUE)!!
)

fun DSLContext.save(stockList: StockList) {
    val toSave = when {
        stockList.items.isEmpty() -> listOf(sentinelItem)
        else -> stockList.items
    }
    toSave.forEach { item ->
        with(ITEMS) {
            insertInto(ITEMS)
                .set(ID, item.id.toString())
                .set(MODIFIED, stockList.lastModified)
                .set(NAME, item.name.toString())
                .set(QUALITY, item.quality.valueInt)
                .set(SELLBYDATE, item.sellByDate)
                .execute()
        }
    }
}

fun DSLContext.load(): StockList {
    with(ITEMS) {
        val records = select(ID, MODIFIED, NAME, QUALITY, SELLBYDATE)
            .from(ITEMS)
            .where(
                MODIFIED.eq(DSL.select(max(MODIFIED)).from(ITEMS))
            )
            .fetch()
        return if (records.isEmpty())
            StockList(Instant.EPOCH, emptyList())
        else {
            val lastModified: Instant = records.first()[MODIFIED]
            val items: List<Item> = records.map { it.toItem() }
            val isEmpty = (items.singleOrNull() == sentinelItem)
            StockList(
                lastModified,
                if (isEmpty) emptyList() else items
            )
        }
    }
}

private fun Record5<String, Instant, String, Int, LocalDate>.toItem() =
    Item(
        id = ID(this[ITEMS.ID]) ?: error("Invalid ID"),
        name = NonBlankString(this[ITEMS.NAME]) ?: error("Invalid name"),
        sellByDate = this[ITEMS.SELLBYDATE],
        quality = Quality(this[ITEMS.QUALITY]) ?: error("Invalid quality")
    )

