package com.gildedrose

import com.gildedrose.domain.Item
import org.http4k.core.Request
import org.http4k.core.body.form

class DeleteItemsHttpTests : DeleteItemsAcceptanceContract(
    alison = HttpActor()
)

class DeleteItemsHttpWithNoHtmxTests : DeleteItemsAcceptanceContract(
    alison = HttpNoHtmxActor()
)

internal fun Request.withDeleteForm(
    toDelete: Set<Item>
) = toDelete.fold(this) { acc, item ->
    acc.form(item.id.toString(), "on")
}

