package com.gildedrose.rendering

import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.PricedStockList
import com.gildedrose.domain.StockListLoadingError
import com.gildedrose.http.ResponseErrors.withError
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
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css") {
            attributes["integrity"] = "sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
            attributes["crossorigin"] = "anonymous"
        }
        script {
            src = "https://unpkg.com/htmx.org@2.0.6"
            integrity = "sha384-Akqfrbj/HpNVo8k11SXBb6TlBWmXXlYQrCSqEWmyKJe+hDm3Z/B2WVG4smwBkRVm"
            attributes["crossorigin"] = "anonymous"
        }
    }
    body {
        div(classes = "container my-4") {
            h1(classes = "h3 mb-3") { +dateFormat.format(LocalDate.ofInstant(now, zoneId)) }
            form {
                method = FormMethod.post
                action = "/add-item"
                attributes["hx-post"] = "/add-item"
                attributes["hx-target"] = "table"
                attributes["hx-swap"] = "outerHTML"
                id = "new-item-form"
            }
            form(classes = "mb-3 text-end") {
                button(classes = "btn btn-danger") {
                    type = ButtonType.submit
                    attributes["hx-post"] = "/delete-items"
                    attributes["hx-target"] = "table"
                    attributes["hx-swap"] = "outerHTML"
                    attributes["hx-confirm"] = "Are you sure you want to delete the items?"
                    attributes["hx-include"] = "table input[type='checkbox']:checked"
                    attributes["aria-label"] = "Delete selected items"
                    +"Delete"
                }
            }
            renderTable(stockList.items, now, zoneId)
        }
    }
}

private fun FlowContent.renderTable(
    items: List<PricedItem>,
    now: Instant,
    zoneId: ZoneId,
    editingId: String? = null,
) {
    table(classes = "table table-striped table-hover table-bordered table-sm align-middle") {
        tr {
            th { +"" }
            th { +"ID" }
            th { +"Name" }
            th { +"Sell By Date" }
            th { +"Sell By Days" }
            th { +"Quality" }
            th { +"Price" }
            th { +"Edit" }
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
                    attributes["class"] = "form-control form-control-sm"
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
                    attributes["class"] = "form-control form-control-sm"
                    attributes["aria-label"] = "New item name"
                }
            }
            td {
                input {
                    form = "new-item-form"
                    type = InputType.date
                    name = "new-itemSellBy"
                    attributes["class"] = "form-control form-control-sm"
                    attributes["aria-label"] = "New item sell by date"
                }
            }
            td { +"" }
            td {
                attributes["class"] = "text-end"
                input {
                    form = "new-item-form"
                    type = InputType.number
                    name = "new-itemQuality"
                    required = true
                    min = "0"
                    size = "3"
                    attributes["class"] = "form-control form-control-sm"
                    attributes["aria-label"] = "New item quality"
                }
            }
            td {
                input(type = InputType.submit) {
                    form = "new-item-form"
                    value = "Add"
                    attributes["class"] = "btn btn-primary w-100 btn-sm"
                    attributes["aria-label"] = "Add new item"
                }
            }
            td { +"" }
        }
        items.forEach { item ->
            if (item.id.toString() == editingId) {
                tr {
                    td { +"" }
                    td { +item.id.toString() }
                    td {
                        input {
                            type = InputType.text
                            name = "edit-itemName"
                            required = true
                            size = "20"
                            value = item.name.value
                            attributes["class"] = "form-control form-control-sm"
                            attributes["aria-label"] = "Edit item name"
                        }
                    }
                    td {
                        input {
                            type = InputType.date
                            name = "edit-itemSellBy"
                            attributes["class"] = "form-control form-control-sm"
                            attributes["aria-label"] = "Edit item sell by date"
                            item.sellByDate?.let { value = it.toString() }
                        }
                    }
                    td { +"" }
                    td {
                        attributes["class"] = "text-end"
                        input {
                            type = InputType.number
                            name = "edit-itemQuality"
                            required = true
                            min = "0"
                            size = "3"
                            value = item.quality.toString()
                            attributes["class"] = "form-control form-control-sm"
                            attributes["aria-label"] = "Edit item quality"
                        }
                    }
                    td {
                        // hidden id so hx-include will submit it
                        input {
                            type = InputType.hidden
                            name = "edit-itemId"
                            value = item.id.toString()
                        }
                        input(type = InputType.submit) {
                            value = "Save"
                            attributes["class"] = "btn btn-primary w-100 btn-sm"
                            attributes["aria-label"] = "Save changes"
                            attributes["hx-post"] = "/edit-item"
                            attributes["hx-target"] = "table"
                            attributes["hx-swap"] = "outerHTML"
                            attributes["hx-include"] = "closest tr"
                        }
                    }
                    td {
                        button(type = ButtonType.button, classes = "btn btn-outline-secondary btn-sm w-100") {
                            attributes["hx-get"] = "/"
                            attributes["hx-target"] = "table"
                            attributes["hx-swap"] = "outerHTML"
                            attributes["aria-label"] = "Cancel edit"
                            +"Cancel"
                        }
                    }
                }
            } else {
                tr {
                    td {
                        input(type = InputType.checkBox, name = item.id.toString()) {
                            attributes["class"] = "form-check-input"
                            attributes["aria-label"] = "Select item"
                        }
                    }
                    td { +item.id.toString() }
                    td { +item.name.value }
                    td { +if (item.sellByDate == null) "" else dateFormat.format(item.sellByDate) }
                    td {
                        attributes["class"] = "text-end"
                        +item.daysUntilSellBy(LocalDate.ofInstant(now, zoneId)).toString()
                    }
                    td {
                        attributes["class"] = "text-end"
                        +item.quality.toString()
                    }
                    td {
                        attributes["class"] = "text-end"
                        +when (val price = item.price) {
                            is Success -> price.value?.toString().orEmpty()
                            is Failure -> "error"
                        }
                    }
                    td {
                        button(type = ButtonType.button, classes = "btn btn-secondary btn-sm w-100") {
                            attributes["hx-get"] = "/edit/${item.id}"
                            attributes["hx-target"] = "table"
                            attributes["hx-swap"] = "outerHTML"
                            +"Edit"
                        }
                    }
                }
            }
        }
    }
}

internal fun renderTableHtml(items: List<PricedItem>, now: Instant, zoneId: ZoneId, editingId: String? = null): String =
    partial { renderTable(items, now, zoneId, editingId) }

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
