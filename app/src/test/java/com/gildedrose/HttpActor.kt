package com.gildedrose

import com.gildedrose.domain.Item
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus

class HttpActor : Actor() {
    override fun delete(fixture: Fixture, items: Set<Item>) {
        val request = Request(Method.POST, "/delete-items")
            .header("HX-Request", "True")
            .withDeleteForm(items)
        val response = fixture.app.routes(request)
        assertThat(
            response,
            hasStatus(Status.OK) and hasJustATableElementBody()
        )
    }
}
