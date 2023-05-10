package com.gildedrose.persistence

import com.gildedrose.db.tables.Items
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.inMemory.InMemoryItems
import com.gildedrose.persistence.jooq.JooqItems
import com.gildedrose.persistence.jooq.testDslContext
import com.gildedrose.testing.IOResolver
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

    private val otherItems = JooqItems(testDslContext)

    val items = DualItems(
        sourceOfTruth = inMemoryItems,
        otherItems = otherItems,
        analytics = events::add
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
    fun `raises event if other items disagrees`() {
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
        assertEquals(1, events.size)
        assertEquals(
            StocklistMismatch(
                Success(initialStockList),
                Success(nullStockist)),
            events.first()
        )
    }
}

