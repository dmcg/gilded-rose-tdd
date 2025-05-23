import com.gildedrose.App
import com.gildedrose.config.Features
import com.gildedrose.http.serverOn
import com.gildedrose.routes

fun main() {
    App(
        dbConfig = dbConfig,
        features = Features(newItemEnabled = true)
    ).apply {
        val port = 8088
        routes.serverOn(port = port).start()
        println("Running test-main at http://localhost:$port/")
    }
}

