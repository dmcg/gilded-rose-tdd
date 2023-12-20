package com.gildedrose.competition

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gildedrose.competition.FetchData.Companion.dataFile
import com.gildedrose.foundation.*
import com.gildedrose.foundation.PropertySets.asLens
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(ApprovalTest::class)
class CompetitionTests {

    private val objectMapper = jacksonObjectMapper()

    private val places = objectMapper.readValue<PropertySet>(dataFile)
        .required<List<PropertySet>>("places")
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

    data class Place(val properties: PropertySet) : PropertySet by properties {
        private val displayNameTextLens = "displayName".asLens() andThen "text".asLens<String>()
        private val addressComponents get() = required<List<PropertySet>>("addressComponents").map(::AddressComponent)

        val displayName = properties.get(displayNameTextLens)
        fun withDisplayName(value: String) = Place(properties.with(displayNameTextLens, value))

        val countryCode: String?
            get() = addressComponents.find { it.types.contains("country") }?.shortText
    }

    data class AddressComponent(val properties: PropertySet) : PropertySet by properties {
        val shortText = required<String>("shortText")
        val types = required<List<String>>("types")
    }
}
