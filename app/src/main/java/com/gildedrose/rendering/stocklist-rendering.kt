package com.gildedrose.rendering

import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.PricedStockList
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*

internal val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK)

fun render(
    stockListResult: Result4k<PricedStockList, StockListLoadingError>,
    now: Instant,
    zoneId: ZoneId,
    justTable: Boolean,
): Response {
    return stockListResult.map { stockList ->
        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(
            renderHtml(
                justTable,
                now,
                stockList,
                zoneId
            )
        )
    }.recover { error ->
        Response(Status.INTERNAL_SERVER_ERROR)
            .withError(error)
            .body("Something went wrong, we're really sorry.")
    }
}

fun renderHtml(
    justTable: Boolean,
    now: Instant,
    stockList: PricedStockList,
    zoneId: ZoneId
): String = if (justTable) {
    partial { renderTable(stockList.items, now, zoneId) }
} else createHTML().html {
    lang = "en"
    head {
        title("Gilded Rose")
        script {
            src = "https://unpkg.com/htmx.org@1.9.4"
            integrity = "sha384-zUfuhFKKZCbHTY6aRR46gxiqszMk5tcHjsVFxnUo8VMus4kHGVdIYVbOYYNlKmHV"
            attributes["crossorigin"] = "anonymous"
        }
    }
    body {
        h1 { +dateFormat.format(LocalDate.ofInstant(now, zoneId)) }
        form {
            method = FormMethod.post
            action = "/add-item"
            attributes["hx-post"] = "/add-item"
            attributes["hx-target"] = "table"
            attributes["hx-swap"] = "outerHTML"
            id = "new-item-form"
        }
        form {
            method = FormMethod.post
            action = "/delete-items"
            attributes["hx-post"] = "/delete-items"
            attributes["hx-target"] = "table"
            attributes["hx-swap"] = "outerHTML"
            attributes["hx-confirm"] = "Are you sure you want to delete the items?"
            input {
                type = InputType.submit
                value = "Delete"
                attributes["aria-label"] = "Delete selected items"
            }
            renderTable(stockList.items, now, zoneId)
        }
    }
}

private fun FlowContent.renderTable(items: List<PricedItem>, now: Instant, zoneId: ZoneId) {
    table {
        tr {
            th { +"" }
            th { +"ID" }
            th { +"Name" }
            th { +"Sell By Date" }
            th { +"Sell By Days" }
            th { +"Quality" }
            th { +"Price" }
        }
        tr {
            td { +"" }
            td {
                input {
                    form = "new-item-form"
                    type = InputType.text
                    name = "new-itemId"
                    required = true
                    size = "5"
                    attributes["aria-label"] = "New item id"
                }
            }
            td {
                input {
                    form = "new-item-form"
                    type = InputType.text
                    name = "new-itemName"
                    required = true
                    size = "20"
                    attributes["aria-label"] = "New item name"
                }
            }
            td {
                input {
                    form = "new-item-form"
                    type = InputType.date
                    name = "new-itemSellBy"
                    attributes["aria-label"] = "New item sell by date"
                }
            }
            td { +"" }
            td {
                style = "text-align: right"
                input {
                    form = "new-item-form"
                    type = InputType.number
                    name = "new-itemQuality"
                    required = true
                    min = "0"
                    size = "3"
                    attributes["aria-label"] = "New item quality"
                }
            }
            td {
                input(type = InputType.submit) {
                    form = "new-item-form"
                    value = "Add"
                    style = "width: 100%"
                    attributes["aria-label"] = "Add new item"
                }
            }
        }
        items.forEach { item ->
            tr {
                td {
                    input(type = InputType.checkBox, name = item.id.toString()) {
                        attributes["aria-label"] = "Select item"
                    }
                }
                td { +item.id.toString() }
                td { +item.name.value }
                td { +if (item.sellByDate == null) "" else dateFormat.format(item.sellByDate) }
                td { style = "text-align: right"; +item.daysUntilSellBy(LocalDate.ofInstant(now, zoneId)).toString() }
                td { style = "text-align: right"; +item.quality.toString() }
                td { style = "text-align: right"; +when (val price = item.price) {
                    is Success -> price.value?.toString().orEmpty()
                    is Failure -> "error"
                } }
            }
        }
    }
}

private fun partial(block: FlowContent.() -> Unit): String {
    val writer = StringWriter()
    val consumer = writer.appendHTML()
    // hacky stuff so we don't have to return a wrapper div
    object : FlowContent {
        override val consumer = consumer
        override val attributes: MutableMap<String, String>
            get() = mutableMapOf()
        override val attributesEntries: Collection<Map.Entry<String, String>>
            get() = emptyList()
        override val emptyTag: Boolean
            get() = true
        override val inlineTag: Boolean
            get() = true
        override val namespace: String?
            get() = null
        override val tagName: String
            get() = ""
    }.block()
    return writer.toString()
}

private fun PricedItem.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null) 0 else
        ChronoUnit.DAYS.between(now, this.sellByDate)
