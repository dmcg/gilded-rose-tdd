package com.gildedrose.persistence

import com.gildedrose.config.DbConfig
import com.gildedrose.db.tables.Items
import org.http4k.cloudnative.env.Environment
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock

val testEnvironment: Environment = Environment.JVM_PROPERTIES overrides
    Environment.ENV overrides
    Environment.from(
        "jdbc.url" to "jdbc:hsqldb:hsql://localhost/gildedrose-test",
        "db.username" to "sa",
        "db.password" to ""
    )

val testDbConfig = DbConfig(testEnvironment)

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
