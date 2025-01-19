package com.gildedrose.competition

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gildedrose.competition.FetchData.Companion.dataFile
import com.gildedrose.foundation.andThen
import com.gildedrose.foundation.asLens
import com.gildedrose.foundation.get
import com.gildedrose.foundation.with
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo


@ExtendWith(ApprovalTest::class)
class StrongCompetitionTests {

    private val objectMapper = jacksonObjectMapper().apply {
        configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val places: List<Place> = objectMapper.readValue<Root>(dataFile).places

    @Test
    fun `process data`(approver: Approver) {
        approver.assertApproved(
            places.joinToString("\n") {
                "${it.displayName.text} ${it.countryCode}"
            }
        )
    }

    @Test
    fun lenses() {
        val displayName = Place::displayName.asLens()
        val text = DisplayName::text.asLens()
        val displayNameText = displayName andThen text

        val aPlace: Place = places.first()

        expectThat(aPlace.displayName.text).isEqualTo("International Magic Shop")
        expectThat(displayNameText.get(aPlace)).isEqualTo("International Magic Shop")

        val editedPlace = aPlace.copy(
            displayName = aPlace.displayName.copy(
                text = "New name"
            )
        )
        expectThat(editedPlace.displayName.text).isEqualTo("New name")

        val editedWithLens = aPlace.with(displayNameText, "New name")
        expectThat(editedWithLens.get(displayNameText)).isEqualTo("New name")

        val transformedPlace = aPlace.copy(
            displayName = aPlace.displayName.copy(
                text = aPlace.displayName.text.uppercase()
            )
        )
        expectThat(transformedPlace.displayName.text).isEqualTo("INTERNATIONAL MAGIC SHOP")

        val transformedWithLens = displayNameText.update(aPlace) { it.uppercase() }
        expectThat(displayNameText.get(transformedWithLens)).isEqualTo("INTERNATIONAL MAGIC SHOP")
    }
}

private val Place.countryCode: String?
    get() = addressComponents.find { it.types.contains("country") }?.shortText




