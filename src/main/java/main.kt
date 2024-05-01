
import com.gildedrose.App
import com.gildedrose.config.toDbConfig
import com.gildedrose.http.serverFor
import com.gildedrose.routes
import org.http4k.cloudnative.env.Environment


val environment = Environment.JVM_PROPERTIES overrides
    Environment.ENV overrides
    Environment.from(
        "jdbc.url" to "jdbc:h2:/tmp/gilded-rose.db",
        "db.username" to "gilded",
        "db.password" to "rose"
    )

val dbConfig = environment.toDbConfig()

fun main() {
    val app = App(dbConfig = dbConfig)
    serverFor(port = 80, app.routes).start()
}
