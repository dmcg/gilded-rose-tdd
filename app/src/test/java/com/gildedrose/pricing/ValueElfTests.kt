package com.gildedrose.pricing

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.foundation.retry
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.resultFrom
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI

@EnabledIfSystemProperty(named = "run-external-tests", matches = "true")
class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
) {
    @Test
    fun `fails sometimes`(fixture: Fixture) {
        val result: List<Result<Price?, Exception>> = (1..500).map {
            resultFrom {
                fixture.client.invoke(fixture.aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == fixture.expectedPrice })
        val successRatio = successes.size / failures.size.toDouble()
        println("Successes = ${successes.size}, failures = ${failures.size}, ratio = $successRatio")
    }

    @Test
    fun `retry prevents failure`(fixture: Fixture) {
        val retryingClient = retry(1, function = { it: Item -> fixture.client(it) })
        val result: List<Result<Price?, Exception>> = (1..500).map {
            resultFrom {
                retryingClient.invoke(fixture.aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == fixture.expectedPrice })
        assertTrue(failures.isEmpty())
    }
}

