package com.gildedrose

import com.gildedrose.domain.Item
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus

class DeleteItemsHttpTests : DeleteItemsAcceptanceContract(
    delete = Fixture::deleteWithHttp
)

private fun Fixture.deleteWithHttp(toDelete: Set<Item>) {
    val request = Request(Method.POST, "/delete-items")
        .header("HX-Request", "True")
        .withDeleteForm(toDelete)
    val response = app.routes(request)
    assertThat(
        response,
        hasStatus(Status.OK) and hasJustATableElementBody()
    )
}

class DeleteItemsHttpWithNoHtmxTests : DeleteItemsAcceptanceContract(
    delete = Fixture::deleteWithHttpNoHtmx
)

private fun Fixture.deleteWithHttpNoHtmx(toDelete: Set<Item>) {
    val request = Request(Method.POST, "/delete-items")
        .withDeleteForm(toDelete)
    val response = app.routes(request)
    assertThat(
        response,
        hasStatus(Status.SEE_OTHER) and hasHeader("Location", "/")
    )
}

private fun Request.withDeleteForm(
    toDelete: Set<Item>
) = toDelete.fold(this) { acc, item ->
    acc.form(item.id.toString(), "on")
}

