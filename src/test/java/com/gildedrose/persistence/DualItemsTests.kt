package com.gildedrose.persistence

import arrow.core.raise.Raise
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.*
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.Failure
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
class DualItemsTests : ItemsContract<DbTxContext>() {

    private val sourceOfTruth = InMemoryItems()
    private val otherItems = DbItems(testDslContext)

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
            items.transactionally { load() }
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

    context(IO)
    @Test
    fun `raises event if other items raises on load`() {
        val error = StockListLoadingError.IOError("Deliberate")
        val brokenOtherItems = object : DbItems(testDslContext) {
            context(IO, DbTxContext, Raise<StockListLoadingError>)
            override fun load(): StockList {
                raise(error)
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
            stocklistLoadingMismatch(
                Success(initialStockList),
                Failure(error)
            ),
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
        val brokenOtherItems = object : DbItems(testDslContext) {
            context(IO, DbTxContext, Raise<StockListLoadingError.IOError>)
            override fun save(stockList: StockList): StockList {
                super.save(stockList)
                return nullStockist
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
    fun `raises event if other items raises on save`() {
        val error = StockListLoadingError.IOError("Deliberate")
        val brokenOtherItems = object : DbItems(testDslContext) {
            context(IO, DbTxContext, Raise<StockListLoadingError.IOError>)
            override fun save(stockList: StockList): StockList {
                raise(error)
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
                Failure(error)
            ),
            events.single()
        )
    }
}

// Turns out, all usages of transactionally expect a Result from it,
// mostly because Items only has save and load, and both can raise a StockListLoadingError
fun <R, TX : TXContext> Items<TX>.transactionally(
    f: context(TX, Raise<StockListLoadingError>) Items<TX>.() -> R
): Result<R, StockListLoadingError> = result4k {
    inTransaction {
        f(magic(), magic<Raise<StockListLoadingError>>(), magic<Items<TX>>())
    }
}



