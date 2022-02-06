import com.gildedrose.Server
import com.gildedrose.loadItems
import com.gildedrose.routes
import com.gildedrose.saveTo
import org.http4k.routing.RoutingHttpHandler
import java.io.File
import java.time.LocalDate

fun main() {
    val file = File("stock.tsv")
    val server = Server(routesFor(file))
    server.start()
}

fun routesFor(
    stockFile: File,
    calender: () -> LocalDate = LocalDate::now
): RoutingHttpHandler {
    val stock = stockFile.loadItems()
    return routes(stock, calender)
}
