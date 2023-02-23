package com.gildedrose.persistence

import org.jetbrains.exposed.sql.Database
import org.postgresql.ds.PGSimpleDataSource

private val testDataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
    portNumbers = intArrayOf(5433)
}

val testDatabase = Database.connect(testDataSource)
