package com.gildedrose.rendering

import com.gildedrose.config.Features
import com.gildedrose.domain.PricedItem
import com.gildedrose.domain.PricedStockList
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.*
import org.http4k.core.*
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK)
private val handlebars = HandlebarsTemplates().HotReload("src/main/java")
private val view = Body.viewModel(handlebars, ContentType.TEXT_HTML).toLens()

fun render(
    stockListResult: Result4k<PricedStockList, StockListLoadingError>,
    now: Instant,
    zoneId: ZoneId,
    features: Features
): Response {
    val today = LocalDate.ofInstant(now, zoneId)
    return stockListResult.map { stockList ->
        Response(Status.OK).with(
            view of
                StockListViewModel(
                    now = dateFormat.format(today),
                    items = stockList.map { item ->
                        val priceString = when (val price = item.price) {
                            is Success -> price.value?.toString().orEmpty()
                            is Failure -> "error"
                        }
                        item.toMap(today, priceString)
                    },
                    isDeletingEnabled = features.isDeletingEnabled
                )
        )
    }.recover { error ->
        Response(Status.INTERNAL_SERVER_ERROR)
            .withError(error)
            .body("Something went wrong, we're really sorry.")
    }
}

private data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>,
    val isDeletingEnabled: Boolean
) : ViewModel

private fun PricedItem.toMap(now: LocalDate, priceString: String): Map<String, String> = mapOf(
    "id" to id.toString(),
    "name" to name.value,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to this.daysUntilSellBy(now).toString(),
    "quality" to this.quality.toString(),
    "price" to priceString
)

private fun PricedItem.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null) 0 else
        ChronoUnit.DAYS.between(now, this.sellByDate)
