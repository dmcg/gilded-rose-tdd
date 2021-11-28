package com.gildedrose

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.StringTemplateSource
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

class Server(
    stock: List<Item>,
    clock: () -> LocalDate = LocalDate::now
) {
    val routes = routes(
        "/" bind Method.GET to { _ ->
            val now = clock()
            Response(Status.OK).body(handlebars(
                StockListViewModel(
                    now = dateFormat.format(now),
                    items = stock.map { it.toMap(now) }
                )
            ))
        }
    )

    private val http4kServer = routes.asServer(Undertow(8080))

    fun start() {
        http4kServer.start()
    }

    private val handlebars = HandlebarsTemplates().HotReload("src/main/java")
}

data class StockListViewModel(
    val now: String,
    val items: List<Map<String, String>>
): ViewModel

private fun Item.toMap(now: LocalDate): Map<String, String> = mapOf(
    "name" to name,
    "sellByDate" to dateFormat.format(sellByDate),
    "sellByDays" to this.daysUntilSellBy(now).toString(),
    "quality" to this.quality.toString()
)

val templateSource = """

""".trimIndent()

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    ChronoUnit.DAYS.between(now, this.sellByDate)
