package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.foundation.Analytics
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.*
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
private val handlebars = HandlebarsTemplates().HotReload("src/main/java")

fun listHandler(
    clock: () -> Instant,
    zoneId: ZoneId,
    analytics: Analytics,
    listing: (Instant) -> Result4k<StockList, StockListLoadingError>
): HttpHandler = { _ ->
    val now = clock()
    val today = LocalDate.ofInstant(now, zoneId)
    when (val stockListResult: Result<StockList, StockListLoadingError> = listing(now)) {
        is Success ->
            Response(OK).body(handlebars(
                StockListViewModel(
                    now = dateFormat.format(today),
                    items = stockListResult.value.map { it.toMap(today) }
                )
            ))
        is Failure -> {
            analytics(stockListResult.reason)
            Response(INTERNAL_SERVER_ERROR).body("Something went wrong, we're really sorry.")
        }
    }
}

private data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>
) : ViewModel

private fun Item.toMap(now: LocalDate): Map<String, String> = mapOf(
    "name" to name,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to this.daysUntilSellBy(now).toString(),
    "quality" to this.quality.toString()
)

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null) 0 else
        ChronoUnit.DAYS.between(now, this.sellByDate)
