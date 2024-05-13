package com.gildedrose.ignoreme

import com.gildedrose.App
import com.gildedrose.config.Features
import com.gildedrose.http.serverFor
import com.gildedrose.routes
import dbConfig
import populateDevDb

fun main() {
    populateDevDb()
    runFakePricing()
    App(
        dbConfig = dbConfig,
        features = Features(newItemEnabled = true)
    ).apply {
        val port = 8088
        serverFor(port = port, routes).start()
        println("Running test-main at http://localhost:$port/")
    }
}

