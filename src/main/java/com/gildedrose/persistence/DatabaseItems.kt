package com.gildedrose.persistence

import com.gildedrose.domain.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate

sealed interface ExposedTx

private object ExposedEvidence : ExposedTx

fun <R> inExposedTransaction(database: Database, block: context(ExposedTx) () -> R): R =
    transaction(database) {
        block(ExposedEvidence)
    }

class DatabaseItems : Items<ExposedTx> {

    context(ExposedTx) override fun save(
        stockList: StockList
    ): Result<StockList, StockListLoadingError.IO> {
        stockList.items.forEach { item ->
            ItemsTable.insert {
                it[id] = item.id.toString()
                it[modified] = stockList.lastModified
                it[name] = item.name.toString()
                it[sellByDate] = item.sellByDate
                it[quality] = item.quality.valueInt
            }
        }
        return Success(stockList)
    }

    context(ExposedTx) override fun load()
        : Result<StockList, StockListLoadingError> =
        // select * from items where modified = (select max(modified) from items)
        getLastUpdate()?.let { lastUpdate ->
            val items = allItemsUpdatedAt(lastUpdate)
            Success(StockList(lastUpdate, items))
        } ?: Success(StockList(Instant.EPOCH, emptyList()))

    private fun getLastUpdate() = ItemsTable
        .slice(ItemsTable.modified.max())
        .selectAll()
        .firstOrNull()
        ?.getOrNull(ItemsTable.modified.max())

    private fun allItemsUpdatedAt(lastUpdate: Instant) = ItemsTable
        .select { ItemsTable.modified eq lastUpdate }
        .map { it.toItem() }

    object ItemsTable : Table() {
        val id: Column<String> = varchar("id", 100)
        val modified: Column<Instant> = timestamp("modified").index()
        val name: Column<String> = varchar("name", 100)
        val sellByDate: Column<LocalDate?> = date("sellByDate").nullable()
        val quality: Column<Int> = integer("quality")
    }

    private fun ResultRow.toItem() =
        Item(
            ID(this[ItemsTable.id])
                ?: error("Could not parse id ${this[ItemsTable.id]}"),
            NonBlankString(this[ItemsTable.name])
                ?: error("Invalid name ${this[ItemsTable.name]}"),
            this[ItemsTable.sellByDate],
            Quality(this[ItemsTable.quality])
                ?: error("Invalid quality ${this[ItemsTable.quality]}")
        )
}

