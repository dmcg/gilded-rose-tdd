package com.gildedrose.persistence

import com.gildedrose.config.DbConfig
import com.gildedrose.config.toDslContext
import com.gildedrose.db.tables.Items
import com.gildedrose.testing.TimingExtension
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import java.net.URI

val testDslContext: DSLContext = DbConfig(
    URI.create("jdbc:postgresql://localhost:5433/gilded-rose"),
    username = "gilded",
    password = "rose"
).toDslContext()

@ResourceLock("DATABASE")
@Order(0)
class DbItemsTests : ItemsContract<DbTxContext>() {
    companion object {
        init {
            TimingExtension.event("DbItemsTests loaded")
        }
    }

    init {
        TimingExtension.event("DbItemsTests ctor1")
    }

    override val items = DbItems(testDslContext)

    init {
        TimingExtension.event("DbItemsTests ctor2")
    }

    @BeforeEach
    fun clearDB() {
        TimingExtension.event("in beforeEach")
        testDslContext.truncate(Items.ITEMS).execute()
    }

    @Test
    override fun transactions() {
        super.transactions()
    }
}
