package com.gildedrose.competition

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gildedrose.foundation.PropertySet
import com.gildedrose.foundation.required
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(ApprovalTest::class)
class CompetitionTests {

    val dataFile = File(
        "src/test/resources/${this.javaClass.packageName.replace('.', '/')}",
        "places-response.json"
    )

    @Disabled("calls Google")
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

    @Test
    fun `process data`(approver: Approver) {
        val places: List<Place> = objectMapper.readValue<PropertySet>(dataFile)
            .required<List<PropertySet>>("places")
            .map(::Place)
        approver.assertApproved(places.joinToString("\n") { "${it.displayName} ${it.countryCode}" })
    }
}

data class Place(val properties: PropertySet) : PropertySet by properties {
    val displayName = required<String>("displayName", "text")
    val countryCode: String? = addressComponents.find { it.types.contains("country") }?.shortText

    private val addressComponents get() = required<List<PropertySet>>("addressComponents").map(::AddressComponent)
}

data class AddressComponent(val properties: PropertySet) : PropertySet by properties {
    val shortText = required<String>("shortText")
    val types = required<List<String>>("types")
}

private val objectMapper = jacksonObjectMapper()

private val GoogleApiKey: String = File("./google-api-key.txt").readText()
