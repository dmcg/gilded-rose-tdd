package com.gildedrose.persistence.jooq

import com.gildedrose.db.tables.Items
import com.gildedrose.foundation.IO
import com.gildedrose.persistence.ItemsContract
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource

val testDataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
    portNumbers = intArrayOf(5433)
}
val testDslContext: DSLContext = DSL.using(testDataSource, SQLDialect.POSTGRES)

class JooqItemsTests : ItemsContract<JooqTXContext>(
    items = JooqItems(testDslContext)
) {

    @BeforeEach
    fun clearDB() {
        testDslContext.truncate(Items.ITEMS).execute()
    }

    context(IO)
    @Test
    override fun transactions() {
        super.transactions()
    }
}
