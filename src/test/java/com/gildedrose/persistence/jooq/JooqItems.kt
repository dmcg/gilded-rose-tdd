package com.gildedrose.persistence.jooq

import com.gildedrose.db.tables.Items.ITEMS
import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.persistence.TXContext
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.Record5
import org.jooq.impl.DSL
import org.jooq.impl.DSL.max
import java.time.Instant
import java.time.LocalDate

class JooqTXContext(val transactionalDSLContext: DSLContext): TXContext()

class JooqItems(
    val dslContext: DSLContext
) : Items<JooqTXContext> {

    override fun <R> inTransaction(block: context(JooqTXContext) () -> R): R =
        dslContext.transactionResult { trx: Configuration ->
            val txContext = JooqTXContext(trx.dsl())
            block(txContext)
        }

    context(IO, JooqTXContext)
    override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IOError> {
        transactionalDSLContext.save(stockList)
        return Success(stockList)
    }

    context(IO, JooqTXContext)
    override fun load(): Result<StockList, StockListLoadingError> {
        return Success(transactionalDSLContext.load())
    }
}

fun DSLContext.save(stockList: StockList) {
    stockList.forEach { item ->
        insertInto(ITEMS)
            .columns(ITEMS.ID, ITEMS.MODIFIED, ITEMS.NAME, ITEMS.QUALITY, ITEMS.SELLBYDATE)
            .values(
                item.id.value.value,
                stockList.lastModified,
                item.name.value,
                item.quality.valueInt,
                item.sellByDate
            ) // TODO("fix value types")
            .execute()
    }
}

fun DSLContext.load(): StockList {
    val items: List<Pair<Instant, Item>> = select(
        ITEMS.ID, ITEMS.MODIFIED, ITEMS.NAME, ITEMS.QUALITY, ITEMS.SELLBYDATE
    )
        .from(ITEMS)
        .where(
            ITEMS.MODIFIED.eq(
                DSL.select(max(ITEMS.MODIFIED)).from(
                    ITEMS
                )
            )
        )
        .fetch()
        .map { record ->
            record[ITEMS.MODIFIED] to record.toItem()
        }
    val lastModified = items.firstOrNull()
    return StockList(lastModified?.first ?: Instant.EPOCH, items.map { it.second })
}


private fun Record5<String, Instant, String, Int, LocalDate>.toItem(): Item {
    val id: ID<Item> = ID(this[ITEMS.ID])!!
    val name = NonBlankString(this[ITEMS.NAME])!!
    val sellByDate = this[ITEMS.SELLBYDATE]
    val quality = Quality(this[ITEMS.QUALITY])!!
    return Item(id, name, sellByDate, quality)
}

