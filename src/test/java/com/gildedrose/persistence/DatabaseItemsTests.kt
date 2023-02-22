package com.gildedrose.persistence

import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach

class DatabaseItemsTests :
    ItemsContract(DatabaseItems(database))
{

    @BeforeEach
    fun resetDB() {
        transaction(database) {
            drop(DatabaseItems.ItemsTable)
            createMissingTablesAndColumns(DatabaseItems.ItemsTable)
        }
    }
}
