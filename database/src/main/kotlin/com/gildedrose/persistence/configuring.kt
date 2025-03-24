package com.gildedrose.persistence

import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
)

fun DbConfig.toDslContext(): DSLContext = DSL.using(
    toHikariDataSource().apply {
        validate()
    },
    SQLDialect.POSTGRES
)

private fun DbConfig.toHikariDataSource() = HikariDataSource().apply {
    jdbcUrl = this@toHikariDataSource.jdbcUrl.toString()
    username = this@toHikariDataSource.username
    password = this@toHikariDataSource.password
}
