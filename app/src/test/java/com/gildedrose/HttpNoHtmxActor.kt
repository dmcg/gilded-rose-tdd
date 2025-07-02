package com.gildedrose

import com.gildedrose.domain.Item
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus

class HttpNoHtmxActor : Actor() {
    override fun delete(fixture: Fixture, items: Set<Item>) {
        val request = Request(Method.POST, "/delete-items")
            .withDeleteForm(items)
        val response = fixture.routes(request)
        assertThat(
            response,
            hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
        )
    }
}
