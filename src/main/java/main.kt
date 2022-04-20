import com.gildedrose.*
import org.http4k.core.HttpHandler
import org.http4k.core.HttpTransaction
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.File
import java.time.Instant
import java.time.ZoneId

fun main() {
    val file = File("stock.tsv")
    val server = Server(
        routesFor(
            stockFile = file,
            clock = { Instant.now() },
            analytics = analytics
        )
    )
    server.start()
}

val analytics = LoggingAnalytics(::println)
private val londonZoneId = ZoneId.of("Europe/London")

fun routesFor(
    stockFile: File,
    clock: () -> Instant,
    analytics: Analytics
): HttpHandler {
    val stock = Stock(stockFile, londonZoneId, ::updateItems)
    return reportHttpTransactions(analytics).then(
        catchAll(analytics).then(
            routes(
                "/" bind GET to listHandler(clock, londonZoneId, stock::stockList),
                "/error" bind GET to { error("deliberate") }
            )
        )
    )
}

private fun reportHttpTransactions(analytics: Analytics) = ResponseFilters.ReportHttpTransaction(
    recordFn = { analytics(HttpEvent(it)) }
)

private fun catchAll(analytics: Analytics) = ServerFilters.CatchAll {
    analytics(UncaughtExceptionEvent(it))
    Response(INTERNAL_SERVER_ERROR).body("Something went wrong, sorry.")
}

data class UncaughtExceptionEvent(
    val message: String,
    val stackTrace: List<String>
) : AnalyticsEvent {
    constructor(exception: Throwable) : this(
        exception.message.orEmpty(),
        exception.stackTrace.map(StackTraceElement::toString)
    )
}

data class HttpEvent(
    val uri: String,
    val method: String,
    val status: Int,
    val latency: Long,
) : AnalyticsEvent {
    constructor(tx: HttpTransaction) : this(
        tx.request.uri.toString(),
        tx.request.method.toString(),
        tx.response.status.code,
        tx.duration.toMillis(),
    )
}
