package com.gildedrose.persistence

import arrow.core.raise.Raise
import com.gildedrose.db.tables.Items.ITEMS
import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.withException
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.Record5
import org.jooq.exception.DataAccessException
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

    override fun <R> inTransaction(block: context(DbTxContext) () -> R): R =
        forInTransaction.untransactionalDSLContext.transactionResult { trx: Configuration ->
            val txContext = DbTxContext(trx.dsl())
            block(txContext)
        }

    context(IO, DbTxContext, Raise<StockListLoadingError.IOError>)
    override fun save(stockList: StockList): StockList = stockList.also { dslContext.save(it) }

    context(IO, DbTxContext, Raise<StockListLoadingError>)
    override fun load(): StockList = dslContext.load()
}

private val sentinelItem = Item(
    id = ID("NO-ITEMS-SAVED")!!,
    name = NonBlankString("THIS IS NOT AN ITEM")!!,
    sellByDate = null,
    quality = Quality(Int.MAX_VALUE)!!
)

context(Raise<StockListLoadingError.IOError>)
fun DSLContext.save(stockList: StockList) = withException(::IOErrorfromJooqException) {
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

context(Raise<StockListLoadingError>)
fun DSLContext.load(): StockList = withException(::IOErrorfromJooqException) {
    with(ITEMS) {
        val records = select(ID, MODIFIED, NAME, QUALITY, SELLBYDATE)
            .from(ITEMS)
            .where(
                MODIFIED.eq(DSL.select(max(MODIFIED)).from(ITEMS))
            )
            .fetch()
        if (records.isEmpty())
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

fun IOErrorfromJooqException(e: DataAccessException): StockListLoadingError.IOError =
    StockListLoadingError.IOError(e.message ?: "Unknown Jooq error")
