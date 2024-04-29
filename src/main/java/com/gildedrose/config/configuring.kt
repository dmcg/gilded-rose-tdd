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
        jdbcUrl = uri(environment),
        username = EnvironmentKey.nonEmptyString().required("db.username")(environment),
        password = EnvironmentKey.nonEmptyString().required("db.password")(environment),
    )
}

private fun uri(environment: Environment): URI {
    val uri = EnvironmentKey.map(URI::create).required("jdbc.url")(environment)
    println(uri)
    return uri
}

fun dslContextFor(dbConfig: DbConfig): DSLContext {
    val dataSource = hikariDataSourceFor(dbConfig)
    dataSource.validate()
    return DSL.using(dataSource, SQLDialect.POSTGRES)
}

fun hikariDataSourceFor(dbConfig: DbConfig) =
    HikariDataSource().apply {
        jdbcUrl = dbConfig.jdbcUrl.toString()
        username = dbConfig.username
        password = dbConfig.password
    }
