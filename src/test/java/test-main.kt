import com.gildedrose.App

fun main() {
    App(port = 8088).apply {
        start()
        println("Running test-main at http://localhost:$port/")
    }
}

