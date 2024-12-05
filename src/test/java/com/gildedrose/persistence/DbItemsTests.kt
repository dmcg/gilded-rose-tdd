package com.gildedrose.persistence

import com.gildedrose.config.DbConfig
import com.gildedrose.config.toDslContext
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.testcontainers.containers.PostgreSQLContainer
import java.net.URI


private val postgres = PostgreSQLContainer("postgres:16-alpine").withUsername("gilded").apply { start() }
private val flyway = Flyway.configure().cleanDisabled(false).dataSource(postgres.jdbcUrl, postgres.username, postgres.password).load()

@ResourceLock("DATABASE")
class DbItemsTests : ItemsContract<DbTxContext>() {
    override val items = DbItems(DbConfig(URI.create(postgres.jdbcUrl), postgres.username, postgres.password).toDslContext())


    @BeforeEach
    fun initDb() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    override fun transactions() {
        super.transactions()
    }
}
