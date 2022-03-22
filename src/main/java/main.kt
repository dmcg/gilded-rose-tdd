import com.gildedrose.*
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
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
): HttpHandler {
    val stock = Stock(stockFile, londonZoneId, ::updateItems)
    return routes(
        "/" bind GET to listHandler(clock, londonZoneId, stock::stockList)
    )
}
