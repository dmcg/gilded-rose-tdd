package com.gildedrose.competition

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

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
    fun `process data`() {
        val data: JsonObject = objectMapper.readValue(dataFile)

        val places: List<JsonObject> = data.required("places")
        places.forEach { place ->
            val addressComponents = place.addressComponents
            val countryComponent = addressComponents.find { component ->
                component.types.contains("country")
            }
            println("${place.displayName} ${countryComponent?.shortText}")
        }
    }
}

private val JsonObject.shortText get() = required<String>("shortText")
private val JsonObject.types get() = required<List<String>>("types")
private val JsonObject.addressComponents get() = required<List<JsonObject>>("addressComponents")
private val JsonObject.displayName get() = required<String>("displayName", "text")

inline fun <reified T : Any> JsonObject.required(key: String): T {
    val value: Any = get(key) ?: error("Cannot find key <$key>")
    return value as? T ?: error("Value for key <$key> is not a ${T::class}")
}

inline fun <reified T : Any> JsonObject.required(key0: String, key1: String): T =
    required<JsonObject>(key0).required<T>(key1)


typealias JsonObject = Map<String, Any?>

private val objectMapper = jacksonObjectMapper()

private val GoogleApiKey: String = File("./google-api-key.txt").readText()
