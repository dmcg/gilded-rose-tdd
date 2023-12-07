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
    private val dataFile = File(
        "src/test/resources/${this.javaClass.packageName.replace('.', '/')}",
        "places-response.json"
    )
    private val places: List<Place> = objectMapper.readValue<Root>(dataFile).places

    private val Place.countryCode: String?
        get() = addressComponents.find { it.types.contains("country") }?.shortText

    @Test
    fun `process data`(approver: Approver) {
        approver.assertApproved(
            places.joinToString("\n") { "${it.displayName.text} ${it.countryCode}" }
        )
    }

    @Test
    fun lenses() {

        val displayName = LensObject<Place, DisplayName>(
            { it.displayName },
            { subject, value -> subject.copy(displayName = value) }
        )

        val text = LensObject<DisplayName, String>(
            { it.text },
            { subject, value -> subject.copy(text = value) }
        )

        val displayNameTextToo = displayName andThen text

        val aPlace: Place = places.first()

        expectThat(aPlace.displayName.text).isEqualTo("International Magic Shop")
        expectThat(displayNameTextToo.get(aPlace)).isEqualTo("International Magic Shop")

        val editedPlace = aPlace.copy(
            displayName = aPlace.displayName.copy(
                text = "New name"
            )
        )
        expectThat(editedPlace.displayName.text).isEqualTo("New name")

        val editedWithLens = displayNameTextToo.inject(aPlace, "New name")
        expectThat(displayNameTextToo.get(editedWithLens)).isEqualTo("New name")

        val transformedPlace = aPlace.copy(
            displayName = aPlace.displayName.copy(
                text = aPlace.displayName.text.uppercase()
            )
        )
        expectThat(transformedPlace.displayName.text).isEqualTo("INTERNATIONAL MAGIC SHOP")

        val transformedWithLens = displayNameTextToo.update(aPlace) { it.uppercase() }
        expectThat(displayNameTextToo.get(transformedWithLens)).isEqualTo("INTERNATIONAL MAGIC SHOP")
    }
}

interface Lens<T, R> {
    fun get(subject: T): R
    fun inject(subject: T, value: R): T
    fun update(subject: T, f: (R) -> R): T = inject(subject, f(get(subject)))
}

infix fun <T1, T2, R> LensObject<T1, T2>.andThen(second: LensObject<T2, R>) = LensObject<T1, R>(
    { second.get(get(it)) },
    { subject, value ->
        inject(
            subject,
            second.inject(get(subject), value)
        )
    }
)

data class LensObject<T, R>(
    val getter: (T) -> R,
    val injector: (T, R) -> T
) : Lens<T, R> {
    override fun get(subject: T) = getter(subject)
    override fun inject(subject: T, value: R) = injector(subject, value)
}




