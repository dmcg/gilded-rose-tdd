package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.runIO
import com.gildedrose.http.ResponseErrors
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.http.catchAll
import com.gildedrose.http.reportHttpTransactions
import com.gildedrose.rendering.render
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.filter.ServerFilters
import org.http4k.lens.*
import org.http4k.lens.ParamMeta.IntegerParam
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Duration


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

internal fun App.addHandler(request: Request): Response {
    val idLens = FormField.nonBlankString().map { ID<Item>(it) }.required("new-itemId")
    val nameLens = FormField.nonBlankString().required("new-itemName")
    val sellByLens = FormField.localDate().optional("new-itemSellBy")
    val qualityLens = FormField.nonNegativeInt().map { Quality(it) }.required("new-itemQuality")
    val formBody = Body.webForm(Validator.Feedback, idLens, nameLens, sellByLens, qualityLens).toLens()
    val form: WebForm = formBody(request)
    if (form.errors.isNotEmpty())
        return Response(Status.BAD_REQUEST).withError(NewItemFailedEvent(form.errors.toString()))

    val item = Item(idLens(form), nameLens(form), sellByLens(form), qualityLens(form))
    runIO {
        addItem(newItem = item)
    }
    return Response(Status.SEE_OTHER).header("Location", "/")
}

data class NewItemFailedEvent(val message: String) : AnalyticsEvent

fun FormField.nonNegativeInt() =
    mapWithNewMeta(
        BiDiMapping<String, NonNegativeInt>(
            { NonNegativeInt(it.toInt()) ?: throw IllegalArgumentException("Integer cannot be negative") },
            NonNegativeInt::toString
        ), IntegerParam
    )

fun FormField.nonBlankString(): BiDiLensSpec<WebForm, NonBlankString> =
    map(BiDiMapping<String, NonBlankString>({ s: String ->
        NonBlankString(s) ?: throw IllegalArgumentException("String cannot be blank")
    }, { it.toString() }))

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

