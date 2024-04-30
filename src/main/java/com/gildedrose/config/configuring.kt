package com.gildedrose.config

import com.gildedrose.foundation.printed
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
        jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(environment).printed(),
        username = EnvironmentKey.nonEmptyString().required("db.username")(environment),
        password = EnvironmentKey.nonEmptyString().required("db.password")(environment),
    )
}

fun DbConfig.toDslContext() =
    toHikariDataSource().toDslContext()

fun DbConfig.toHikariDataSource() =
    HikariDataSource().also {
        it.jdbcUrl = jdbcUrl.toString()
        it.username = username
        it.password = password
        it.validate()
    }

private fun HikariDataSource.toDslContext() =
    DSL.using(this, SQLDialect.POSTGRES)
