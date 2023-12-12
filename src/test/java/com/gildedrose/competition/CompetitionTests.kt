package com.gildedrose.competition

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gildedrose.competition.FetchData.Companion.dataFile
import com.gildedrose.foundation.PropertySet
import com.gildedrose.foundation.required
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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

    data class Place(val properties: PropertySet) : PropertySet by properties {
        val displayName = required<String>("displayName", "text")
        val countryCode: String? = addressComponents.find { it.types.contains("country") }?.shortText

        private val addressComponents get() = required<List<PropertySet>>("addressComponents").map(::AddressComponent)
    }

    data class AddressComponent(val properties: PropertySet) : PropertySet by properties {
        val shortText = required<String>("shortText")
        val types = required<List<String>>("types")
    }
}


