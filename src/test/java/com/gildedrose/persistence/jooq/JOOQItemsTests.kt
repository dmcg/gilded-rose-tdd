package com.gildedrose.persistence.jooq

import com.gildedrose.db.tables.Items
import com.gildedrose.persistence.ItemsContract
import com.gildedrose.persistence.NoTX
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource

val testDataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
    portNumbers = intArrayOf(5433)
}
val dslContext: DSLContext = DSL.using(testDataSource, SQLDialect.POSTGRES)

class JOOQItemsTests : ItemsContract<NoTX>(
    items = JOOQItems(dslContext)
) {

    @BeforeEach
    fun clearDB() {
        dslContext.truncate(Items.ITEMS).execute()
    }
}
