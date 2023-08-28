package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.foundation.IO
import com.gildedrose.testing.IOResolver
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.extension.ExtendWith

context(IO)
@ExtendWith(IOResolver::class)
class DeleteItemsHttpTests : DeleteItemsAcceptanceContract(
    doDelete = ::deleteWithHttp
)

private fun deleteWithHttp(app: App, toDelete: Set<Item>) {
    val request = Request(Method.POST, "/delete-items")
        .header("HX-Request", "True")
        .withDeleteForm(toDelete)
    val response = app.routes(request)
    assertThat(
        response,
        hasStatus(Status.OK) and hasJustATableElementBody()
    )
}

context(IO)
@ExtendWith(IOResolver::class)
class DeleteItemsHttpWithNoHtmxTests : DeleteItemsAcceptanceContract(
    doDelete = ::deleteWithHttpNoHtmx
)

private fun deleteWithHttpNoHtmx(app: App, toDelete: Set<Item>) {
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

