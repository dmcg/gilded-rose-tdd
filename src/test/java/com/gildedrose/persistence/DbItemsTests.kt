package com.gildedrose.persistence

import com.gildedrose.config.DbConfig
import com.gildedrose.config.toDslContext
import com.gildedrose.db.tables.Items
import com.gildedrose.testing.TestTiming
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import java.net.URI

val testDslContext: DSLContext = DbConfig(
    URI.create("jdbc:postgresql://localhost:5433/gilded-rose"),
    username = "gilded",
    password = "rose"
).toDslContext().also {
    TestTiming.event("created test DSL context")
}

@Disabled
@ResourceLock("DATABASE")
@Order(0)
class DbItemsTests : ItemsContract<DbTxContext>() {
    companion object {
        init {
            TestTiming.event("DbItemsTests loaded")
        }
    }

    override val items = run {
        TestTiming.event("create >")
        DbItems(testDslContext).also {
            TestTiming.event("< create")
        }
    }

    @BeforeEach
    fun clearDB() {
        TestTiming.event("truncate >")
        testDslContext.truncate(Items.ITEMS).execute()
        TestTiming.event("< truncate")
    }

    @Test
    fun one() {
        TestTiming.event("one >")
        `returns empty stocklist before any save`()
        TestTiming.event("< one")
    }

    @Test
    fun two() {
        TestTiming.event("two >")
        `returns empty stocklist before any save`()
        TestTiming.event("< two")
    }

//    @Test
    override fun transactions() {
        super.transactions()
    }
}
