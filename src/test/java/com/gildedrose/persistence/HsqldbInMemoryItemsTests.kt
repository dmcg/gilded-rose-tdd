package com.gildedrose.persistence

import org.flywaydb.core.Flyway
import org.hsqldb.jdbc.JDBCDataSource
import org.junit.jupiter.api.BeforeAll
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
    val inMemoryDataSource = JDBCDataSource().apply {
        setURL("jdbc:hsqldb:mem:${UUID.randomUUID()}")
        user = "sa"
    }

    @BeforeEach
    fun migrateDatabase() {
        inMemoryDataSource.migrateSchema()
    }

    override val items = HsqldbItems(inMemoryDataSource)
}


@ResourceLock("DATABASE")
class HsqldbItemsClientTests : ItemsContract<ConnectionContext>() {
    companion object {
        val serverDataSource = JDBCDataSource().apply {
            setURL("jdbc:hsqldb:hsql://localhost/gildedrose-test")
            user = "sa"
        }

        @JvmStatic
        @BeforeAll
        fun createSchema() {
            serverDataSource.migrateSchema()
        }
    }

    @BeforeEach
    fun migrateDatabase() {
        serverDataSource.clearTables()
    }

    override val items = HsqldbItems(serverDataSource)
}

