package com.gildedrose.persistence.jooq

import org.postgresql.ds.PGSimpleDataSource


private val testDataSource = PGSimpleDataSource().apply {
    user = "gilded"
    password = "rose"
    databaseName = "gilded-rose"
    portNumbers = intArrayOf(5433)
}

class JOOQTests {

}
