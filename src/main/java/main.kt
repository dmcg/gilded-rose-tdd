import com.gildedrose.App
import com.gildedrose.config.DbConfig
import com.gildedrose.http.serverFor
import com.gildedrose.routes
import org.http4k.config.Environment
import org.http4k.config.Environment.Companion.ENV
import org.http4k.config.Environment.Companion.JVM_PROPERTIES

fun main() {
    App(dbConfig)
        .routes
        .serverFor(port = 80)
        .start()
}

private val localEnv = Environment.from(
    "jdbc.url" to "jdbc:h2:/tmp/gilded-rose.db",
    "db.username" to "gilded",
    "db.password" to "rose"
)

val dbConfig = DbConfig(JVM_PROPERTIES overrides ENV overrides localEnv)
