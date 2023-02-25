package com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach

class DatabaseItemsTests : ItemsContract<ExposedTx>(
    items = DatabaseItems(),
    inTransaction = object : InTransaction<ExposedTx> {
        override fun <R> invoke(block: context(ExposedTx) () -> R): R = inExposedTransaction(testDatabase, block)
    }
) {

    @BeforeEach
    fun resetDB() {
        transaction(testDatabase) {
            drop(DatabaseItems.ItemsTable)
            createMissingTablesAndColumns(DatabaseItems.ItemsTable)
        }
    }
}
