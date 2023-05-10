package com.gildedrose.persistence.jooq

import com.gildedrose.config.toDbConfig
import com.gildedrose.config.toHikariDataSource
import com.gildedrose.db.tables.Items
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.ItemsContract
import org.http4k.cloudnative.env.Environment
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
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

val testDslContext: DSLContext = run {
    DSL.using(
        testEnvironment.toDbConfig().toHikariDataSource().apply {
            validate()
        },
        SQLDialect.POSTGRES
    )
}

context(IO)
@ResourceLock("DATABASE")
class JooqItemsTests : ItemsContract<JooqTXContext>(
    items = JooqItems(testDslContext)
) {

    @BeforeEach
    fun clearDB() {
        testDslContext.truncate(Items.ITEMS).execute()
    }

    @Test
    override fun transactions() {
        super.transactions()
    }
}
