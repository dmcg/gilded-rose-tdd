package com.gildedrose.persistence.jooq

import com.gildedrose.db.tables.Items
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.ItemsContract
import dev.forkhandles.result4k.Success
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.test.assertEquals

val testDataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
    portNumbers = intArrayOf(5433)
}
val dslContext: DSLContext = DSL.using(testDataSource, SQLDialect.POSTGRES)

class JooqItemsTests : ItemsContract<JooqTXContext>(
    items = JooqItems(dslContext)
) {

    @BeforeEach
    fun clearDB() {
        dslContext.truncate(Items.ITEMS).execute()
    }

    context(IO)
    @Test
    fun transactions() {
        val cyclicBarrier = CyclicBarrier(2)
        val thread = thread {
            items.inTransaction {
                items.save(initialStockList)
                cyclicBarrier.await()
                cyclicBarrier.await()
            }
        }

        cyclicBarrier.await()
        items.inTransaction {
            assertEquals(Success(nullStockist), items.load())
        }

        cyclicBarrier.await()
        thread.join()
        items.inTransaction {
            assertEquals(Success(initialStockList), items.load())
        }
    }
}
