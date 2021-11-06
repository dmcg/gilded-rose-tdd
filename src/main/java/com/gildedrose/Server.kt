package com.gildedrose

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.io.StringTemplateSource
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Server(
    stock: List<Item>,
    clock: () -> LocalDate = LocalDate::now
) {
    val routes = routes(
        "/" bind Method.GET to { _ ->
            val now = clock()
            Response(Status.OK).body(rootTemplate.apply(
                stock.map { it.toMap(now) }
            ))
        }
    )

    private val http4kServer = routes.asServer(Undertow(8080))

    fun start() {
        http4kServer.start()
    }

    val handlebars = Handlebars()
    val rootTemplate = handlebars.compile(
        StringTemplateSource("no such file", templateSource)
    )

}

private fun Item.toMap(now: LocalDate): Map<String, String> = mapOf(
    "name" to name,
    "sellByDate" to dateFormat.format(sellByDate),
    "sellByDays" to this.daysUntilSellBy(now).toString(),
    "quality" to this.quality.toString()
)

val templateSource = """
    <html lang="en">
    <body>
    <table>
    {{#each}}<tr>
        <td>{{this.name}}</td>
        <td>{{this.sellByDate}}</td>
        <td>{{this.sellByDays}}</td>
        <td>{{this.quality}}</td>
    </tr>
    {{/each}}
    </table>
    </body>
    </html>
""".trimIndent()

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    ChronoUnit.DAYS.between(now, this.sellByDate)
