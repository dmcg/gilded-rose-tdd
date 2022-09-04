import com.gildedrose.*
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.http.serverFor
import java.io.File
import java.time.Instant

fun main() {
    val features = Features()
    val file = File("stock.tsv")
    val server = serverFor(
        routesFor(
            stockFile = file,
            clock = { Instant.now() },
            pricing = ::dummyPricing,
            analytics = analytics,
            features
        )
    )
    server.start()
}

@Suppress("UNUSED_PARAMETER")
fun dummyPricing(item: Item): Price? = null
