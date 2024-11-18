package com.gildedrose.persistence

import com.gildedrose.config.toDbConfig
import com.gildedrose.config.toDslContext
import com.gildedrose.db.tables.Items
import org.http4k.config.Environment
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock

val testEnvironment: Environment = Environment.JVM_PROPERTIES overrides
    Environment.ENV overrides
    Environment.from(
        "jdbc.url" to "jdbc:postgresql://localhost:5433/gilded-rose",
        "db.username" to "gilded",
        "db.password" to "rose"
    )

val testDbConfig = testEnvironment.toDbConfig()

val testDslContext: DSLContext = testDbConfig.toDslContext()

@ResourceLock("DATABASE")
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
