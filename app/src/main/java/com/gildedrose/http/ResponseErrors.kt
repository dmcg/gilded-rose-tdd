package com.gildedrose.http

import com.gildedrose.foundation.AnalyticsEvent
import org.http4k.core.Filter
import org.http4k.core.Response
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ResponseErrors : ResponseErrorsImpl()

// This class just exists so that we have a non-singleton version to use in tests
open class ResponseErrorsImpl(
    private val headerName: String = "x-http4k-error-id",
    private val lookup: MutableMap<String, AnalyticsEvent> = ConcurrentHashMap<String, AnalyticsEvent>()
) {

    fun Response.withError(error: AnalyticsEvent): Response {
        val uuid = UUID.randomUUID().toString()
        lookup[uuid] = error
        return this.header(headerName, uuid)
    }

    val Response.attachedError: AnalyticsEvent? get() = header(headerName)?.let { lookup[it] }

    fun reportTo(analytics: (AnalyticsEvent) -> Unit): Filter = Filter { next ->
        { request ->
            val base = next(request)
            base.header(headerName)?.let { uuid ->
                try {
                    lookup[uuid]?.let {
                        analytics(it)
                    }
                } finally {
                    lookup.remove(uuid)
                }
                base.removeHeader(headerName)
            } ?: base
        }
    }
}
