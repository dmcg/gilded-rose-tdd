package com.gildedrose.config

import com.zaxxer.hikari.HikariDataSource
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonEmptyString
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI
import javax.sql.DataSource

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
)

fun Environment.toDbConfig() = DbConfig(
    jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(this),
    username = EnvironmentKey.nonEmptyString().required("db.username")(this),
    password = EnvironmentKey.nonEmptyString().required("db.password")(this),
)

fun DbConfig.toDslContext(): DSLContext =
    HikariDataSource()
        .configureFrom(this)
        .also { it.validate() }
        .toDslContext()

private fun HikariDataSource.configureFrom(dbConfig: DbConfig): HikariDataSource {
    jdbcUrl = dbConfig.jdbcUrl.toString()
    username = dbConfig.username
    password = dbConfig.password
    return this
}

private fun DataSource.toDslContext() =
    DSL.using(this, SQLDialect.POSTGRES)
