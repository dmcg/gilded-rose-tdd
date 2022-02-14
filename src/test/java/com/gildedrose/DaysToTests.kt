package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.*


class DaysToTests {

    private val london = ZoneId.of("Europe/London")

    @Test
    fun `no daylight savings in Jan`() {
        assertEquals(0, time("2022-01-01T00:00:00Z").daysTo(time("2022-01-01T00:00:00Z"), london))

        assertEquals(0, time("2022-01-01T00:00:00Z").daysTo(time("2022-01-01T23:59:59Z"), london))
        assertEquals(0, time("2022-01-01T23:59:59Z").daysTo(time("2022-01-01T00:00:00Z"), london))

        assertEquals(1, time("2022-01-01T23:59:59Z").daysTo(time("2022-01-02T00:00:00Z"), london))
        assertEquals(-1, time("2022-01-02T00:00:00Z").daysTo(time("2022-01-01T23:59:59Z"), london))
    }

    @Test
    fun `daylight savings in June`() {
        assertEquals(0, time("2022-06-01T00:00:00Z").daysTo(time("2022-06-01T00:00:00Z"), london))

        assertEquals(1, time("2022-06-01T00:00:00Z").daysTo(time("2022-06-01T23:59:59Z"), london))
        assertEquals(-1, time("2022-06-01T23:59:59Z").daysTo(time("2022-06-01T00:00:00Z"), london))

        assertEquals(0, time("2022-06-01T23:59:59Z").daysTo(time("2022-06-02T00:00:00Z"), london))
        assertEquals(0, time("2022-06-02T00:00:00Z").daysTo(time("2022-06-01T23:59:59Z"), london))
    }

    @Test
    fun `daylight savings June parsed as GMT+1`() {
        // Should have same results as January
        assertEquals(0, time("2022-06-01T00:00:00+01:00").daysTo(time("2022-06-01T00:00:00+01:00"), london))

        assertEquals(0, time("2022-06-01T00:00:00+01:00").daysTo(time("2022-06-01T23:59:59+01:00"), london))
        assertEquals(0, time("2022-06-01T23:59:59+01:00").daysTo(time("2022-06-01T00:00:00+01:00"), london))

        assertEquals(1, time("2022-06-01T23:59:59+01:00").daysTo(time("2022-06-02T00:00:00+01:00"), london))
        assertEquals(-1, time("2022-06-02T00:00:00+01:00").daysTo(time("2022-06-01T23:59:59+01:00"), london))
    }

    @Test
    fun `daylight savings June parsed as local`() {
        // Should have same results as January
        assertEquals(0, local("2022-06-01T00:00:00").daysTo(local("2022-06-01T00:00:00"), london))

        assertEquals(0, local("2022-06-01T00:00:00").daysTo(local("2022-06-01T23:59:59"), london))
        assertEquals(0, local("2022-06-01T23:59:59").daysTo(local("2022-06-01T00:00:00"), london))

        assertEquals(1, local("2022-06-01T23:59:59").daysTo(local("2022-06-02T00:00:00"), london))
        assertEquals(-1, local("2022-06-02T00:00:00").daysTo(local("2022-06-01T23:59:59"), london))
    }

    @Test
    fun `daylight savings transitions`() {
        // Times where how we interpret the time make a difference to the day
        assertEquals(1, local("2022-10-29T23:59:59").daysTo(local("2022-10-30T00:00:01"), london))
        assertEquals(-1, local("2022-10-30T00:00:01").daysTo(local("2022-10-29T23:59:59"), london))

        assertEquals(1, local("2022-03-27T23:59:59").daysTo(local("2022-03-28T00:00:01"), london))
        assertEquals(-1, local("2022-03-28T00:00:01").daysTo(local("2022-03-27T23:59:59"), london))

        assertEquals(1, time("2022-03-27T22:59:59Z").daysTo(time("2022-03-27T23:00:01Z"), london))
        assertEquals(0, time("2022-03-27T23:00:01Z").daysTo(time("2022-03-28T00:00:00Z"), london))
    }

    @Test
    fun `throw things at it`() {
        val zoneIds = listOf(
            ZoneId.of("Europe/London"),
            ZoneId.of("Australia/Sydney")
        )

        val interestingMonths = listOf(1, 3, 10, 12)

        val localDates: Sequence<LocalDate> =
            (1970..2040).asSequence().flatMap { year ->
                interestingMonths.flatMap { month ->
                    (1..31).map { day ->
                        LocalDate.of(year, month, day)
                    }
                }
            }

        val timesBeforeMidnight = listOf(
            LocalTime.of(0, 0, 0),
            LocalTime.of(0, 0, 1),
            LocalTime.of(12, 0, 0),
            LocalTime.of(23, 0, 0),
            LocalTime.of(23, 59, 59),
        )

        val timesAfterMidnight = listOf(
            LocalTime.of(0, 0, 0),
            LocalTime.of(0, 0, 1),
            LocalTime.of(1, 0, 0),
            LocalTime.of(2, 0, 0),
        )

        val daysToAdd = listOf(-365, -2L, -1L, 1L, 2L, 30L, 365L, 366L)


        zoneIds.forEach { zoneId ->
            localDates.forEach { startDate ->
                daysToAdd.forEach { plusDays ->
                    val endDate = startDate.plusDays(plusDays)
                    timesBeforeMidnight.forEach { startTime ->
                        val startDateTime = startDate.atTime(startTime)
                        timesAfterMidnight.forEach { endTime ->
                            val endDateTime = endDate.atTime(endTime)
                            val startInstant = startDateTime.toInstantIn(zoneId)
                            val endInstant = endDateTime.toInstantIn(zoneId)

                            assertEquals(plusDays, startInstant.daysTo(endInstant, zoneId))
                            assertEquals(-plusDays, endInstant.daysTo(startInstant, zoneId))
                        }
                    }
                }
            }
        }
    }

    private fun local(s: String): Instant = LocalDateTime.parse(s).toInstantIn(london)
}

private fun time(s: String): Instant = OffsetDateTime.parse(s).toInstant()
private fun LocalDateTime.toInstantIn(zoneId: ZoneId): Instant = ZonedDateTime.of(this, zoneId).toInstant()
