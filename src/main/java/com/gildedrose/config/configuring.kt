package com.gildedrose.config

import com.gildedrose.testing.TestTiming
import com.zaxxer.hikari.HikariDataSource
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.nonEmptyString
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
)

fun DbConfig.toDslContext(): DSLContext {
    val apply = toHikariDataSource().apply {
        validate()
    }
    TestTiming.event("hikari created")
    return DSL.using(
        apply,
        SQLDialect.POSTGRES
    )
}


fun Environment.toDbConfig() = DbConfig(
    jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(this),
    username = EnvironmentKey.nonEmptyString().required("db.username")(this),
    password = EnvironmentKey.nonEmptyString().required("db.password")(this),
)

fun DbConfig.toHikariDataSource() = HikariDataSource().apply {
    jdbcUrl = this@toHikariDataSource.jdbcUrl.toString()
    username = this@toHikariDataSource.username
    password = this@toHikariDataSource.password
}
