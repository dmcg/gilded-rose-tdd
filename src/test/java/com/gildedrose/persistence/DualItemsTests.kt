package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.test.assertEquals


context(IO)
@ResourceLock("DATABASE")
@ExtendWith(IOResolver::class)
class DualItemsTests : ItemsContract<JooqTXContext>() {

    private val sourceOfTruth = InMemoryItems()
    private val otherItems = JooqItems(testDslContext)

    private val events = mutableListOf<Any>()
    private val analytics: Analytics = { events.add(it) }
    private val printingAnalytics = analytics then
        loggingAnalytics(::println)

    override val items = DualItems(sourceOfTruth, otherItems, printingAnalytics)

    @BeforeEach
    fun clearDB() {
        testDslContext.truncate(com.gildedrose.db.tables.Items.ITEMS).execute()
    }

    context(IO)
    @Test
    fun `returns result from source of truth`() {
        sourceOfTruth.transactionally { save(initialStockList) }
        otherItems.transactionally { save(initialStockList) }
        assertEquals(
            Success(initialStockList),
            items.transactionally { items.load() }
        )
        assertEquals(0, events.size)
    }

    context(IO)
    @Test
    fun `raises event if other items disagrees on load`() {
        sourceOfTruth.transactionally { save(initialStockList) }
        otherItems.transactionally { save(nullStockist) }
        assertEquals(
            Success(initialStockList),
            items.transactionally { items.load() }
        )
        assertEquals(
            stocklistLoadingMismatch(
                Success(initialStockList),
                Success(nullStockist)
            ),
            events.single()
        )
    }

    context(IO)
    @Test
    fun `raises event if other items throws on load`() {
        val exception = RuntimeException("Deliberate")
        val brokenOtherItems = object : JooqItems(testDslContext) {
            context(IO, JooqTXContext)
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

    context(IO)
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

    context(IO)
    @Test
    fun `raises event if other items disagrees on save`() {
        val brokenOtherItems = object : JooqItems(testDslContext) {
            context(IO, JooqTXContext)
            override fun save(stockList: StockList)
                : Result<StockList, StockListLoadingError.IOError> {
                super.save(stockList)
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

    context(IO)
    @Test
    fun `raises event if other items throws on save`() {
        val exception = RuntimeException("Deliberate")
        val brokenOtherItems = object : JooqItems(testDslContext) {
            context(IO, JooqTXContext)
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

private fun <R, TX : TXContext> Items<TX>.transactionally(f: context(TX) Items<TX>.() -> R): R =
    inTransaction {
        f(magic(), this)
    }



