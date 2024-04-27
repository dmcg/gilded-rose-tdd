package com.gildedrose.config

import com.zaxxer.hikari.HikariDataSource
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonEmptyString
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
) {
    constructor(environment: Environment) : this(
        jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(environment),
        username = EnvironmentKey.nonEmptyString().required("db.username")(environment),
        password = EnvironmentKey.nonEmptyString().required("db.password")(environment),
    )
}

fun dslContextFor(dbConfig: DbConfig): DSLContext {
    val dataSource = hikariDataSourceFor(dbConfig)
    dataSource.validate()
    return DSL.using(dataSource, SQLDialect.POSTGRES)
}

fun hikariDataSourceFor(dbConfig: DbConfig): HikariDataSource {
    val result = HikariDataSource()
    result.jdbcUrl = dbConfig.jdbcUrl.toString()
    result.username = dbConfig.username
    result.password = dbConfig.password
    return result
}
