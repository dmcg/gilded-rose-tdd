package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.loggingAnalytics
import com.gildedrose.foundation.magic
import com.gildedrose.foundation.then
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.test.assertEquals


@ResourceLock("DATABASE")
class DualItemsTests : ItemsContract<ConnectionContext>() {
    companion object {
        val dataSource = testDbConfig.toDataSource()

        @JvmStatic
        @BeforeAll
        fun setup() {
            dataSource.migrateSchema()
        }

        @JvmStatic
        @AfterAll
        fun closeDataSource() {
            dataSource.close()
        }
    }

    private val sourceOfTruth = InMemoryItems()
    private val otherItems = HsqldbItems(dataSource)

    private val events = mutableListOf<Any>()
    private val analytics: Analytics = { events.add(it) }
    private val printingAnalytics = analytics then
        loggingAnalytics(::println)

    override val items = DualItems(sourceOfTruth, otherItems, printingAnalytics)

    @BeforeEach
    fun clearDB() {
        dataSource.clearTables()
    }


    @Test
    fun `returns result from source of truth`() {
        sourceOfTruth.transactionally { save(initialStockList) }
        otherItems.transactionally { save(initialStockList) }
        assertEquals(
            Success(initialStockList),
            items.transactionally { load() }
        )
        assertEquals(0, events.size)
    }

    @Test
    fun `raises event if other items disagrees on load`() {
        sourceOfTruth.transactionally { save(initialStockList) }
        otherItems.transactionally { save(nullStockist) }
        assertEquals(
            Success(initialStockList),
            items.transactionally { load() }
        )
        assertEquals(
            stocklistLoadingMismatch(
                Success(initialStockList),
                Success(nullStockist)
            ),
            events.single()
        )
    }

    @Test
    fun `raises event if other items throws on load`() {
        val exception = RuntimeException("Deliberate")
        val brokenOtherItems = object : Items<ConnectionContext> by HsqldbItems(dataSource) {
            context(ConnectionContext)
            override fun load():
                Result<StockList, StockListLoadingError> {
                throw exception
            }
        }
        val items = DualItems(
            sourceOfTruth,
            brokenOtherItems,
            analytics = printingAnalytics
        )
        sourceOfTruth.transactionally { save(initialStockList) }
        assertEquals(
            Success(initialStockList),
            items.transactionally { load() }
        )
        assertEquals(
            StockListLoadingExceptionCaught(exception),
            events.single()
        )
    }

    @Test
    fun `saves to both items`() {
        assertEquals(
            Success(initialStockList),
            items.transactionally { save(initialStockList) }
        )
        assertEquals(
            Success(initialStockList),
            sourceOfTruth.transactionally { load() }
        )
        assertEquals(
            Success(initialStockList),
            otherItems.transactionally { load() }
        )
    }

    @Test
    fun `raises event if other items disagrees on save`() {
        val dbItems = HsqldbItems(dataSource)

        val brokenOtherItems = object : Items<ConnectionContext> by dbItems {
            context(ConnectionContext)
            override fun save(stockList: StockList)
                : Result<StockList, StockListLoadingError.IOError> {
                dbItems.save(stockList)
                return Success(nullStockist)
            }
        }
        val items = DualItems(
            sourceOfTruth,
            brokenOtherItems,
            analytics = printingAnalytics
        )
        assertEquals(
            Success(initialStockList),
            items.transactionally { save(initialStockList) }
        )
        assertEquals(
            stocklistSavingMismatch(
                Success(initialStockList),
                Success(nullStockist)
            ),
            events.single()
        )
    }

    @Test
    fun `raises event if other items throws on save`() {
        val exception = RuntimeException("Deliberate")
        val brokenOtherItems = object : Items<ConnectionContext> by HsqldbItems(dataSource) {
            context(ConnectionContext)
            override fun save(stockList: StockList)
                : Result<StockList, StockListLoadingError.IOError> {
                throw exception
            }
        }
        val items = DualItems(
            sourceOfTruth,
            brokenOtherItems,
            analytics = printingAnalytics
        )
        assertEquals(
            Success(initialStockList),
            items.transactionally { save(initialStockList) }
        )
        assertEquals(
            StockListSavingExceptionCaught(exception),
            events.single()
        )
    }
}

fun <R, TX : TXContext> Items<TX>.transactionally(f: context(TX) Items<TX>.() -> R): R =
    inTransaction {
        f(magic(), this)
    }



