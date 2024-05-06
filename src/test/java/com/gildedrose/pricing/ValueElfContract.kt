package com.gildedrose.pricing

import com.gildedrose.Item
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.net.URI
import java.time.LocalDate.now

@ExtendWith(FixtureResolver::class)
abstract class ValueElfContract(
    override val fixture: Fixture,
) : FixtureResolver.FixtureSource {

    data class Fixture(
        val uri: URI,
        val handler: HttpHandler,
        val expectedPrice: Price,
        val aFoundItem: Item = Item("banana", "doesn't matter", now(), 9),
        val aNotFoundItem: Item = Item("no-such", "doesn't matter", now(), 9)
    ) {
        val client = valueElfClient(uri, handler)
    }

    context(Fixture)
    @Test
    fun `returns price when there is one`() {
        assertEquals(expectedPrice, client(aFoundItem))
    }

    context(Fixture)
    @Test
    fun `returns null when no price`() {
        assertEquals(null, client(aNotFoundItem))
    }

    context(Fixture)
    @Test
    fun `returns BAD_REQUEST for invalid query strings`() {
        val request = Request(Method.GET, uri.toString())
        val returnsBadRequest: Matcher<Response> = hasStatus(BAD_REQUEST)
        check(request, returnsBadRequest)
        check(request.query("id", "some-id"), returnsBadRequest)
        check(request.query("id", ""), returnsBadRequest)
        check(request.query("id", "some-id").query("quality", ""), returnsBadRequest)
        check(request.query("id", "some-id").query("quality", "nan"), returnsBadRequest)
        check(request.query("id", "some-id").query("quality", "-1"), returnsBadRequest)
    }
}

private fun ValueElfContract.Fixture.check(request: Request, matcher: Matcher<Response>) {
    assertThat(handler(request), matcher)
}

class FixtureResolver : ParameterResolver {

    interface FixtureSource {
        val fixture: Any
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ) = parameterContext.parameter.type == ValueElfContract.Fixture::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ) = (extensionContext.requiredTestInstance as? FixtureSource)?.fixture
        ?: error("Test is not a FixtureSource")
}
