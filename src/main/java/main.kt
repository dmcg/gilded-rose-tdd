import com.gildedrose.App
import com.gildedrose.config.toDbConfig
import org.http4k.cloudnative.env.Environment


val environment = Environment.JVM_PROPERTIES overrides
    Environment.ENV overrides
    Environment.from(
        "jdbc.url" to "jdbc:postgresql://localhost:5432/gilded-rose",
        "db.username" to "gilded",
        "db.password" to "rose"
    )

val dbConfig = environment.toDbConfig()

fun main() {
    App(dbConfig = dbConfig).start()
}
