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

    fun dslContext(): DSLContext {
        val dataSource = hikariDataSource(this)
        return DSL.using(dataSource, SQLDialect.H2)
    }
}

private fun hikariDataSource(dbConfig: DbConfig): HikariDataSource {
    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = dbConfig.jdbcUrl.toString()
    dataSource.username = dbConfig.username
    dataSource.password = dbConfig.password
    dataSource.validate()
    return dataSource
}



