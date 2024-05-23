package com.gildedrose.config

import com.zaxxer.hikari.HikariDataSource
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonEmptyString
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.net.URI
import javax.sql.DataSource

data class DbConfig(
    val jdbcUrl: URI,
    val username: String,
    val password: String
)

fun dbConfigFrom(environment: Environment) = DbConfig(
    jdbcUrl = environment[EnvironmentKey.map(URI::create).required("jdbc.url")],
    username = environment[EnvironmentKey.nonEmptyString().required("db.username")],
    password = environment[EnvironmentKey.nonEmptyString().required("db.password")],
)

fun DbConfig.toDslContext() =
    hikariDataSourceFor(this).asDslContext()

private fun DataSource.asDslContext() =
    DSL.using(this, SQLDialect.H2)

private fun hikariDataSourceFor(dbConfig: DbConfig) =
    HikariDataSource().apply {
        jdbcUrl = dbConfig.jdbcUrl.toString()
        username = dbConfig.username
        password = dbConfig.password
    }.also {
        it.validate()
    }




