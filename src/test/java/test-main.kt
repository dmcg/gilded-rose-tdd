import com.gildedrose.App
import com.gildedrose.Features
import com.gildedrose.foundation.retry
import com.gildedrose.pricing.valueElfClient
import java.net.URI

fun main() {
    App(
        port = 8088,
        features = Features(pricing = true),
        pricing = retry(1, function = valueElfClient(URI.create("http://value-elf.com:8080/prices")))
    ).apply {
        start()
        println("Running test-main at http://localhost:$port/")
    }
}

