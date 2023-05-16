package com.gildedrose.persistence

import com.gildedrose.db.tables.Items
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import com.gildedrose.persistence.inMemory.InMemoryItems
import com.gildedrose.persistence.jooq.JooqItems
import com.gildedrose.persistence.jooq.JooqTXContext
import com.gildedrose.persistence.jooq.testDslContext
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.test.assertEquals


@ResourceLock("DATABASE")
@ExtendWith(IOResolver::class)
class DualItemsTests() {
    private val inMemoryItems = InMemoryItems()
    private val events = mutableListOf<Any>()
    val analytics: Analytics =  { events.add(it) }
    val checkingAnalytics = analytics then
        loggingAnalytics(::println)

    private val otherItems = JooqItems(testDslContext)

    val items = DualItems(
        sourceOfTruth = inMemoryItems,
        otherItems = otherItems,
        analytics = checkingAnalytics
    )

    @BeforeEach
    fun clearDB() {
        testDslContext.truncate(Items.ITEMS).execute()
    }

    context(IO)
    @Test
    fun `returns result from source of truth`() {
        inMemoryItems.inTransaction {
            inMemoryItems.save(initialStockList)
        }
        otherItems.inTransaction {
            otherItems.save(initialStockList)
        }
        assertEquals(
            Success(initialStockList),
            items.inTransaction { items.load() }
        )
        assertEquals(0, events.size)
    }

    context(IO)
    @Test
    fun `raises event if other items disagrees on load`() {
        inMemoryItems.inTransaction {
            inMemoryItems.save(initialStockList)
        }
        otherItems.inTransaction {
            otherItems.save(nullStockist)
        }
        assertEquals(
            Success(initialStockList),
            items.inTransaction { items.load() }
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
        val brokenOtherItems = object: JooqItems(testDslContext) {
            context(IO, JooqTXContext) override fun load(): Result<StockList, StockListLoadingError> {
                throw exception
            }
        }
        val items = DualItems(
            inMemoryItems,
            brokenOtherItems,
            analytics = checkingAnalytics
        )
        inMemoryItems.inTransaction {
            inMemoryItems.save(initialStockList)
        }
        assertEquals(
            Success(initialStockList),
            items.inTransaction { items.load() }
        )
        assertEquals(
            StockListLoadingExceptionCaught(exception),
            events.single()
        )
    }

    context(IO)
    @Test
    fun `saves to both items`() {
        items.inTransaction {
            val saved = items.save(initialStockList)
            assertEquals(Success(initialStockList), saved)
        }
        inMemoryItems.inTransaction {
            assertEquals(
                Success(initialStockList),
                inMemoryItems.load()
            )
        }
        otherItems.inTransaction {
            assertEquals(
                Success(initialStockList),
                otherItems.load()
            )
        }
    }

    context(IO)
    @Test
    fun `raises event if other items disagrees on save`() {
        val brokenOtherItems = object: JooqItems(testDslContext) {
            context(IO, JooqTXContext)
            override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IOError> {
                super.save(stockList)
                return Success(nullStockist)
            }
        }
        val items = DualItems(
            inMemoryItems,
            brokenOtherItems,
            analytics = checkingAnalytics
        )
        // setup to have otherItems return nullStockList from save
        assertEquals(
            Success(initialStockList),
            items.inTransaction { items.save(initialStockList) }
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
        val brokenOtherItems = object: JooqItems(testDslContext) {
            context(IO, JooqTXContext) override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IOError> {
                throw exception
            }
        }
        val items = DualItems(
            inMemoryItems,
            brokenOtherItems,
            analytics = checkingAnalytics
        )
        assertEquals(
            Success(initialStockList),
            items.inTransaction { items.save(initialStockList) }
        )
        assertEquals(
            StockListSavingExceptionCaught(exception),
            events.single()
        )
    }
}

