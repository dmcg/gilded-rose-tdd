package com.gildedrose.competition

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File


@ExtendWith(ApprovalTest::class)
class StrongCompetitionTests {

    private val objectMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val dataFile = File(
        "src/test/resources/${this.javaClass.packageName.replace('.', '/')}",
        "places-response.json"
    )

    @Test
    fun `process data`(approver: Approver) {
        val places: List<Place> = objectMapper.readValue<Root>(dataFile).places
        approver.assertApproved(places.joinToString("\n") { "${it.displayName.text} ${it.countryCode}" })

        val place = places.first()

        val place2 =
            place.copy(paymentOptions = place.paymentOptions.copy(acceptsNfc = !place.paymentOptions.acceptsNfc))
        val place3 = acceptsNfc(place) { !it }
        expectThat(place2).isEqualTo(place3)
    }

    private val Place.countryCode: String?
        get() = addressComponents.find { it.types.contains("country") }?.shortText
}

interface Lens<T, V> : (T) -> V {
    operator fun invoke(subject: T, value: V): T
    fun inject(subject: T, value: V): T = this(subject, value)

    operator fun invoke(subject: T, f: (V) -> V): T {
        val current = this(subject)
        val updated = f(current)
        return this.inject(subject, updated)
    }
}

object acceptsNfc : Lens<Place, Boolean> {
    override operator fun invoke(subject: Place): Boolean = subject.paymentOptions.acceptsNfc
    override operator fun invoke(subject: Place, value: Boolean): Place =
        subject.withPaymentOptions { copy(acceptsNfc = value) }
}

private fun Place.withPaymentOptions(f: (PaymentOptions).() -> PaymentOptions): Place =
    this.copy(paymentOptions = f(this.paymentOptions))

