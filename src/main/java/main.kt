import com.gildedrose.Server
import com.gildedrose.loadItems
import java.io.File

fun main() {
    val file = File("stock.tsv").also { it.createNewFile() }
    val stock = file.loadItems()

    val server = Server(stock)
    server.start()
}
