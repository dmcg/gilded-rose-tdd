package com.gildedrose.persistence

import com.gildedrose.db.tables.Items.ITEMS
import com.gildedrose.domain.Item
import com.gildedrose.domain.ItemName
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
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
        val untransactionalDSLContext = dslContext
    }

    override fun <R> inTransaction(block: context(DbTxContext) () -> R): R =
        forInTransaction.untransactionalDSLContext.transactionResult { trx: Configuration ->
            val txContext = DbTxContext(trx.dsl())
            block(txContext)
        }

    context(DbTxContext)
    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError> {
        dslContext.save(stockList)
        return Success(stockList)
    }

    context(DbTxContext)
    override fun load(): Result<StockList, StockListLoadingError> {
        return Success(dslContext.load())
    }
}

private val sentinelItem = Item(
    id = "NO-ITEMS-SAVED",
    name = ItemName("THIS IS NOT AN ITEM")!!,
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
            insertInto(this)
                .set(ID, item.id)
                .set(MODIFIED, stockList.lastModified)
                .set(NAME, item.name.value)
                .set(QUALITY, item.quality.valueInt)
                .set(SELLBYDATE, item.sellByDate)
                .execute()
        }
    }
}

fun DSLContext.load(): StockList {
    val records = select(ITEMS.ID, ITEMS.MODIFIED, ITEMS.NAME, ITEMS.QUALITY, ITEMS.SELLBYDATE)
        .from(ITEMS)
        .where(
            ITEMS.MODIFIED.eq(DSL.select(max(ITEMS.MODIFIED)).from(ITEMS))
        )
        .fetch()
    return if (records.isEmpty())
        StockList(Instant.EPOCH, emptyList())
    else {
        val lastModified: Instant = records.first()[ITEMS.MODIFIED]
        val items: List<Item> = records.map { it.toItem() }
        val isEmpty = (items.singleOrNull() == sentinelItem)
        StockList(
            lastModified,
            if (isEmpty) emptyList() else items
        )
    }
}

private fun Record5<String, Instant, String, Int, LocalDate>.toItem() =
    Item(
        id = this[ITEMS.ID].ifBlank { error("Invalid ID") },
        name = ItemName(this[ITEMS.NAME]) ?: error("Invalid name"),
        sellByDate = this[ITEMS.SELLBYDATE],
        quality = Quality(this[ITEMS.QUALITY]) ?: error("Invalid quality")
    )

