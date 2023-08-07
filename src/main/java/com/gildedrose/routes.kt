package com.gildedrose

import arrow.core.raise.*
import com.gildedrose.domain.*
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.magic
import com.gildedrose.foundation.runIO
import com.gildedrose.http.ResponseErrors
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.rendering.render
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.http4k.core.*
import org.http4k.core.body.Form
import org.http4k.core.body.form
import org.http4k.filter.ServerFilters
import org.http4k.lens.*
import org.http4k.routing.bind
import org.http4k.routing.routes

val App.routes: HttpHandler
    get() = ServerFilters.RequestTracing()
        .then(reportHttpTransactions(Duration.ofSeconds(1), analytics))
        .then(catchAll(analytics))
        .then(ResponseErrors.reportTo(analytics))
        .then(
            routes(
                "/" bind Method.GET to ::listHandler,
                "/error" bind Method.GET to { error("deliberate") },
                "/delete-items" bind Method.POST to ::deleteHandler,
                "/add-item" bind Method.POST to ::addHandler
            )
        )

internal fun App.addHandler(request: Request): Response = recover({
    val form = request.form()
    val item = Item(
        form.required("new-itemId") { ID(it) },
        form.required("new-itemName") { NonBlankString(it) },
        form.optional("new-itemSellBy") { it?.ifEmpty { null }?.toLocalDate() },
        form.required("new-itemQuality") { Quality(it.toIntSafe()) })
    runIO { addItem(newItem = item) }
    Response(Status.SEE_OTHER).header("Location", "/")
}) { error ->
    Response(Status.BAD_REQUEST).withError(NewItemFailedEvent(error))
}

data class NewItemFailedEvent(val message: String) : AnalyticsEvent

context(Raise<String>)
fun Form.required(name: String): String =
    findSingle(name) ?: raise("formData '$name' is required")

fun Form.optional(name: String): String? = findSingle(name)

// The following 2 functions take care of including the invalid field name in the error message.
context(Raise<String>)
fun <T> Form.required(name: String, transform: context(Raise<String>) (String) -> T): T {
    val value = required(name)
    return withError({ e -> "formData '$name': $e" }) {
        transform(magic(), value)
    }
}

context(Raise<String>)
fun <T> Form.optional(name: String, transform: context(Raise<String>) (String?) -> T): T {
    val value = optional(name)
    return withError({ e -> "formData '$name': $e" }) {
        transform(magic(), value)
    }
}

// In a Raise world, these would already be defined instead of their exception-throwing counterparts
context(Raise<String>)
fun String.toLocalDate(): LocalDate =
    // This catch function is reified! So it only catches DateTimeParseException
    catch<DateTimeParseException, _>({ LocalDate.parse(this) }) { raise("Invalid date format") }

context(Raise<String>)
fun String.toIntSafe(): Int =
    catch<NumberFormatException, _>({ toInt() }) { raise("Invalid number format") }

private fun App.listHandler(
    @Suppress("UNUSED_PARAMETER") request: Request
): Response =
    runIO {
        val now = this.clock()
        val stockListResult = loadStockList(now)
        render(stockListResult, now, londonZoneId, this.features)
    }

private fun App.deleteHandler(
    request: Request
): Response {
    runIO {
        val itemIds = request.form().map { it.first }.mapNotNull { ID<Item>(it) }.toSet()
        deleteItemsWithIds(itemIds)
        return Response(Status.SEE_OTHER).header("Location", "/")
    }
}

