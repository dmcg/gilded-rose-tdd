package com.gildedrose.competition

import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotBlank
import java.io.File

class CompetitionTests {

    @Test
    fun `reads api key`() {
        expectThat(GoogleApiKey).isNotBlank()
    }

    @Disabled("calls Google")
    @Test
    fun `fetches data`() {
        val client = ApacheClient()
        val query = "Magical Goods"
        val request = Request(Method.POST, "https://places.googleapis.com/v1/places:searchText")
            .header("X-Goog-Api-Key", GoogleApiKey)
            .header("X-Goog-FieldMask", "*")
            .body("""{ "textQuery" : "$query"}""")
        val response = client(request).bodyString()
        println(response)
    }
}

private val GoogleApiKey: String = File("./google-api-key.txt").readText()
