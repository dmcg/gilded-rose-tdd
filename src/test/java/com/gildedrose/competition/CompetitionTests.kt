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
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

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
    fun `nullable lenses`() {
        val aPropertySet = emptyMap<String, Any?>()

        expectThrows<NoSuchElementException> {
            val aLens = "propertyName".asLens<String>()
            aPropertySet.get(aLens)
        }

        val aLens = "propertyName".asLens<String?>()
        expectThat(aPropertySet[aLens]).isNull()
        val updated = aPropertySet.with(aLens, "a value")
        expectThat(updated).isEqualTo(mapOf("propertyName" to "a value"))

        val updated2 = updated.updatedWith(aLens) { it?.uppercase() }
        expectThat(updated2).isEqualTo(mapOf("propertyName" to "A VALUE"))

        val reverted = updated2.with(aLens, null)
        expectThat(reverted).isEqualTo(mapOf("propertyName" to null))
    }

    data class Place(val properties: PropertySet) : PropertySet by properties {
        private val displayNameTextLens = "displayName".asLens() andThen "text".asLens<String>()
        private val addressComponents get() = valueOf<List<PropertySet>>("addressComponents").map(::AddressComponent)

        val displayName = properties[displayNameTextLens]
        fun withDisplayName(value: String) = Place(properties.with(displayNameTextLens, value))

        val countryCode: String?
            get() = addressComponents.find { it.types.contains("country") }?.shortText
    }

    data class AddressComponent(val properties: PropertySet) : PropertySet by properties {
        val shortText = valueOf<String>("shortText")
        val types = valueOf<List<String>>("types")
    }
}
