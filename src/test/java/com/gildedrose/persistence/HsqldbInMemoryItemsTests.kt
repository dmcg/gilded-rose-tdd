package com.gildedrose.persistence

import org.flywaydb.core.Flyway
import org.hsqldb.jdbc.JDBCDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import java.util.UUID


private fun JDBCDataSource.migrateSchema() {
    val flyway = Flyway.configure()
        .locations("filesystem:src/main/resources/db/migration")
        .dataSource(this)
        .load()
    flyway.migrate()
}

fun JDBCDataSource.clearTables() {
    connection.use { c ->
        c.createStatement().use { s ->
            s.execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK")
        }
    }
}


@ResourceLock("DATABASE")
class HsqldbItemsInMemoryTests : ItemsContract<ConnectionContext>() {
    val dataSource = JDBCDataSource().apply {
        setURL("jdbc:hsqldb:mem:${UUID.randomUUID()}")
        user = "sa"
    }

    @BeforeEach
    fun migrateDatabase() {
        dataSource.migrateSchema()
    }

    override val items = HsqldbItems(dataSource)

    @Test
    override fun transactions() {
        super.transactions()
    }
}
