import com.gildedrose.Server
import com.gildedrose.loadItems
import com.gildedrose.printout
import java.io.File
import java.time.LocalDate

fun main() {
    val file = File("stock.tsv").also { it.createNewFile() }
    val stock = file.loadItems()

    val server = Server(stock)
    server.start()
}
