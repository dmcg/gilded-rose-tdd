package com.gildedrose.config

import com.gildedrose.persistence.DbConfig
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.nonEmptyString
import java.net.URI

fun Environment.toDbConfig() = DbConfig(
    jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(this),
    username = EnvironmentKey.nonEmptyString().required("db.username")(this),
    password = EnvironmentKey.nonEmptyString().required("db.password")(this),
)
