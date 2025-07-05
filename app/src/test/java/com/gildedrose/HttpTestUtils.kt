package com.gildedrose

import com.gildedrose.domain.Item
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.body.form
import org.http4k.hamkrest.hasBody

internal fun postFormToAddItemsRoute(withHTMX: Boolean = true) =
    Request(Method.POST, "/add-item").header("Content-Type", "application/x-www-form-urlencoded").run {
        if (withHTMX) header("HX-Request", "true") else this
    }

internal fun Request.addFormFor(newItem: Item): Request {
    return form("new-itemId", newItem.id.toString())
        .form("new-itemName", newItem.name.toString())
        .form("new-itemSellBy", newItem.sellByDate?.toString() ?: "")
        .form("new-itemQuality", newItem.quality.toString())
}

fun hasJustATableElementBody() = hasBody(Regex("""\A\s*<table>.*</table>\s*\z""", RegexOption.DOT_MATCHES_ALL))
