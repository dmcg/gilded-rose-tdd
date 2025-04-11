
import com.gildedrose.App
import com.gildedrose.http.serverFor
import com.gildedrose.persistence.DbConfig
import com.gildedrose.routes
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.nonEmptyString
import java.net.URI


val environment = Environment.JVM_PROPERTIES overrides
    Environment.ENV overrides
    Environment.from(
        "jdbc.url" to "jdbc:postgresql://localhost:5432/gilded-rose",
        "db.username" to "gilded",
        "db.password" to "rose"
    )

val dbConfig = environment.toDbConfig()

fun main() {
    val app = App(dbConfig = dbConfig)
    serverFor(port = 80, app.routes).start()
}

private fun Environment.toDbConfig() = DbConfig(
    jdbcUrl = EnvironmentKey.map(URI::create).required("jdbc.url")(this),
    username = EnvironmentKey.nonEmptyString().required("db.username")(this),
    password = EnvironmentKey.nonEmptyString().required("db.password")(this),
)
