package com.gildedrose.competition

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gildedrose.competition.FetchData.Companion.dataFile
import com.gildedrose.foundation.*
import com.gildedrose.foundation.PropertySets.asLens
import com.gildedrose.foundation.PropertySets.lens
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(ApprovalTest::class)
class CompetitionTests {

    private val objectMapper = jacksonObjectMapper()

    private val places = objectMapper.readValue<PropertySet>(dataFile)
        .valueOf<List<PropertySet>>("places")
        .map(::Place)

    @Test
    fun `process data`(approver: Approver) {
        approver.assertApproved(
            places.joinToString("\n") {
                "${it.displayName} ${it.countryCode}"
            }
        )
    }

    @Test
    fun update() {
        val aPlace = Place(
            mapOf(
                "displayName" to mapOf("text" to "International Magic Shop"),
            )
        )
        expectThat(aPlace.displayName).isEqualTo("International Magic Shop")
        val updatedPlace = aPlace.withDisplayName("New value")
        expectThat(aPlace.displayName).isEqualTo("International Magic Shop")
        expectThat(updatedPlace.displayName).isEqualTo("New value")
    }

    @Test
    fun `propertySet valueOf`() {
        val aPropertySet = mapOf(
            "key" to "value",
            "null" to null
        )

        expectThat(aPropertySet.valueOf<String?>("no-such")).isNull()
        expectCatching { aPropertySet.valueOf<String>("no-such") }
            .isFailure()
            .isA<IllegalStateException>()
            .message.isEqualTo("Value for key <no-such> is null")

        expectThat(aPropertySet.valueOf<String?>("key")).isEqualTo("value")
        expectCatching { aPropertySet.valueOf<Int?>("key") }
            .isFailure()
            .isA<IllegalStateException>()
            .message.isEqualTo("Value for key <key> is not a class kotlin.Int")

        expectThat(aPropertySet.valueOf<String?>("null")).isNull()
        expectCatching { aPropertySet.valueOf<String>("null") }
            .isFailure()
            .isA<IllegalStateException>()
            .message.isEqualTo("Value for key <null> is null")

    }

    @Test
    fun `nullable properties`() {
        val aPropertySet = emptyMap<String, Any?>()

        expectCatching {
            val aLens = "propertyName".asLens<String>()
            aPropertySet.get(aLens)
        }.isFailure().isA<IllegalStateException>()

        val aLens = "propertyName".asLens<String?>()
        expectThat(aPropertySet.get(aLens)).isNull()
        val updated = aPropertySet.with(aLens, "a value")
        expectThat(updated).isEqualTo(mapOf("propertyName" to "a value"))

        val updated2 = updated.updatedWith(aLens) { it?.uppercase() }
        expectThat(updated2).isEqualTo(mapOf("propertyName" to "A VALUE"))

        val reverted = updated2.with(aLens, null)
        expectThat(reverted).isEqualTo(mapOf("propertyName" to null))
    }

    data class Place(val properties: PropertySet) : PropertySet by properties {
        private val displayNameTextLens = lens("displayName") andThen "text".asLens<String>()
        private val addressComponents get() = valueOf<List<PropertySet>>("addressComponents").map(::AddressComponent)

        val displayName = properties.get(displayNameTextLens)
        fun withDisplayName(value: String) = Place(properties.with(displayNameTextLens, value))

        val countryCode: String?
            get() = addressComponents.find { it.types.contains("country") }?.shortText
    }

    data class AddressComponent(val properties: PropertySet) : PropertySet by properties {
        val shortText = valueOf<String>("shortText")
        val types = valueOf<List<String>>("types")
    }
}
