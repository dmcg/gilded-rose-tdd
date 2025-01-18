package com.gildedrose.persistence

import com.gildedrose.config.DbConfig
import com.gildedrose.config.toDslContext
import com.gildedrose.db.tables.Items
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
).toDslContext()

@Disabled
@ResourceLock("DATABASE")
@Order(0)
class DbItemsTests : ItemsContract<DbTxContext>() {

    override val items = DbItems(testDslContext)

    @BeforeEach
    fun clearDB() {
        testDslContext.truncate(Items.ITEMS).execute()
    }

    @Test
    override fun transactions() {
        super.transactions()
    }
}
