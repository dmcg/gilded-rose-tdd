import com.gildedrose.*
import com.gildedrose.http.Server
import java.io.File
import java.time.Instant

fun main() {
    val features = Features()
    val file = File("stock.tsv")
    val server = Server(
        routesFor(
            stockFile = file,
            clock = { Instant.now() },
            analytics = analytics,
            features
        )
    )
    server.start()
}
