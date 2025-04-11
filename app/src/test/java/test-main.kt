import com.gildedrose.App
import com.gildedrose.Features
import com.gildedrose.http.serverFor
import com.gildedrose.routes

fun main() {
    App(
        dbConfig = dbConfig,
        features = Features()
    ).apply {
        val port = 8088
        serverFor(port = port, routes).start()
        println("Running test-main at http://localhost:$port/")
    }
}

