import com.gildedrose.Item
import com.gildedrose.Server
import com.gildedrose.Stock
import com.gildedrose.listHandler
import org.http4k.core.Method.GET
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.File
import java.time.Instant
import java.time.ZoneId

fun main() {
    val file = File("stock.tsv")
    val server = Server(routesFor(file) { Instant.now() })
    server.start()
}

private val londonZoneId = ZoneId.of("Europe/London")

fun routesFor(
    stockFile: File,
    clock: () -> Instant
): RoutingHttpHandler {
    val stock = Stock(stockFile, londonZoneId, ::updateItems)
    return routes(
        "/" bind GET to listHandler(clock, londonZoneId, stock::stockList)
    )
}

private fun updateItems(items: List<Item>, days: Int) = items.map { it.copy(quality = it.quality - days.toUInt()) }
