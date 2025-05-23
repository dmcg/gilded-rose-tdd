import com.gildedrose.App
import com.gildedrose.config.DbConfig
import com.gildedrose.config.Features
import com.gildedrose.foundation.Analytics
import com.gildedrose.http.serverOn
import com.gildedrose.persistence.DbItems
import com.gildedrose.pricing.valueElfClient
import com.gildedrose.routes
import com.gildedrose.stdOutAnalytics
import org.http4k.config.Environment
import org.http4k.config.Environment.Companion.ENV
import org.http4k.config.Environment.Companion.JVM_PROPERTIES
import java.net.URI
import java.time.Instant

fun main() {
    App()
        .routes
        .serverOn(port = 80)
        .start()
}

fun App(
    dbConfig: DbConfig = DbConfig(JVM_PROPERTIES overrides ENV overrides localEnv),
    features: Features = Features(),
    valueElfUri: URI = URI.create("http://value-elf.com:8080/prices"),
    clock: () -> Instant = Instant::now,
    analytics: Analytics = stdOutAnalytics
) = App(
    DbItems(dbConfig.toDslContext()),
    valueElfClient(valueElfUri),
    clock,
    analytics,
    features
)

private val localEnv = Environment.from(
    "jdbc.url" to "jdbc:h2:/tmp/gilded-rose.db",
    "db.username" to "gilded",
    "db.password" to "rose"
)
