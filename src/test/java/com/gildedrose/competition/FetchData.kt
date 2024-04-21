package com.gildedrose.competition

import java.io.File

class FetchData {
    companion object {
        val dataFile = File(
            "src/test/resources/${this::class.java.packageName.replace('.', '/')}",
            "places-response.json"
        )
    }
}
