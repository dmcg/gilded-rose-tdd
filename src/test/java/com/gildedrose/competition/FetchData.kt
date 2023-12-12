package com.gildedrose.competition

import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("calls Google")
class FetchData {

    @Test
    fun `fetch data`() {
        val client = ApacheClient()
        val query = "magical goods store europe"
        val request = Request(Method.POST, "https://places.googleapis.com/v1/places:searchText")
            .header("X-Goog-Api-Key", GoogleApiKey)
            .header("X-Goog-FieldMask", "*")
            .body("""{ "textQuery" : "$query"}""")
        val response = client(request)
        dataFile.writeText(response.bodyString())
    }

    companion object {
        private val GoogleApiKey: String = File("./google-api-key.txt").readText()
        val dataFile = File(
            "src/test/resources/${this::class.java.packageName.replace('.', '/')}",
            "places-response.json"
        )
    }
}
