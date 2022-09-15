package com.gildedrose.pricing

import com.gildedrose.domain.Price
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI

@EnabledIfSystemProperty(named = "run-external-tests", matches = "true")
class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
)

