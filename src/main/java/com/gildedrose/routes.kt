package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.ItemName
import com.gildedrose.domain.NonNegativeInt
import com.gildedrose.domain.Quality
import com.gildedrose.foundation.AnalyticsEvent
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
    val idLens = FormField.nonBlankString().required("new-itemId")
    val nameLens = FormField.string().required("new-itemName")
    val sellByLens = FormField.localDate().optional("new-itemSellBy")
    val qualityLens = FormField.nonNegativeInt().map { Quality(it) }.required("new-itemQuality")
    val formBody = Body.webForm(Validator.Feedback, idLens, nameLens, sellByLens, qualityLens).toLens()
    val form: WebForm = formBody(request)
    if (form.errors.isNotEmpty())
        return Response(Status.BAD_REQUEST).withError(NewItemFailedEvent(form.errors.toString()))

    val item = Item(idLens(form), ItemName(nameLens(form)), sellByLens(form), qualityLens(form))
    addItem(newItem = item)
    return when {
        request.isHtmx -> listHandler(request)
        else -> Response(Status.SEE_OTHER).header("Location", "/")
    }
}

data class NewItemFailedEvent(val message: String) : AnalyticsEvent

fun FormField.nonNegativeInt() =
    mapWithNewMeta(
        BiDiMapping<String, NonNegativeInt>(
            { NonNegativeInt(it.toInt()) ?: throw IllegalArgumentException("Integer cannot be negative") },
            NonNegativeInt::toString
        ), IntegerParam
    )

//fun FormField.nonBlankString(): BiDiLensSpec<WebForm, NonBlankString> =
//    map(BiDiMapping<String, NonBlankString>({ s: String ->
//        NonBlankString(s) ?: throw IllegalArgumentException("String cannot be blank")
//    }, { it.toString() }))

private fun App.listHandler(
    request: Request
): Response {
    val now = this.clock()
    val stockListResult = this.loadStockList(now)
    return render(stockListResult, now, londonZoneId, this.features, request.isHtmx)
}

private fun App.deleteHandler(
    request: Request
): Response {
    val itemIds = request.form().map { it.first }.toSet()
    this.deleteItemsWithIds(itemIds)
    return when {
        request.isHtmx -> this.listHandler(request)
        else -> Response(Status.SEE_OTHER).header("Location", "/")
    }
}

private val Request.isHtmx: Boolean get() = header("HX-Request") != null


