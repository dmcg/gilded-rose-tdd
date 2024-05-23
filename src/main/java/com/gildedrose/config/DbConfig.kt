package com.gildedrose.config

import com.zaxxer.hikari.HikariDataSource
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonEmptyString
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

fun DbConfig.toDslContext() =
    hikariDataSourceFor(this)
        .let { DSL.using(it, SQLDialect.H2) }

private fun hikariDataSourceFor(dbConfig: DbConfig) =
    HikariDataSource().apply {
        jdbcUrl = dbConfig.jdbcUrl.toString()
        username = dbConfig.username
        password = dbConfig.password
    }.also {
        it.validate()
    }




