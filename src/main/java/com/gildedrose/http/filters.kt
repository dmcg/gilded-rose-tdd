package com.gildedrose.http

import com.gildedrose.foundation.Analytics
import com.gildedrose.foundation.AnalyticsEvent
import com.gildedrose.foundation.UncaughtExceptionEvent
import org.http4k.core.HttpTransaction
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters

fun reportHttpTransactions(analytics: Analytics) = ResponseFilters.ReportHttpTransaction(
    recordFn = { analytics(HttpEvent(it)) }
)

fun catchAll(analytics: Analytics) = ServerFilters.CatchAll {
    analytics(UncaughtExceptionEvent(it))
    Response(Status.INTERNAL_SERVER_ERROR).body("Something went wrong, sorry.")
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
