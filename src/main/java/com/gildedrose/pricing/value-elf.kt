package com.gildedrose.pricing

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.domain.Quality
import com.gildedrose.foundation.IO
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.lens.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.net.URI

fun valueElfClient(uri: URI): context(IO) (Item) -> Price? = valueElfClient(uri, valueElfHttpClient(30))

private fun valueElfHttpClient(simultaneousConnections: Int) = ApacheClient(
    HttpClientBuilder.create().setConnectionManager(
        PoolingHttpClientConnectionManager().apply {
            maxTotal = simultaneousConnections
            defaultMaxPerRoute = simultaneousConnections
        }
    ).build()
)


fun valueElfClient(
    uri: URI,
    client: HttpHandler
): context(IO) (Item) -> Price? {
    return { item ->
        val request = Request(Method.GET, uri.toString())
            .with(
                idLens of item.id,
                qualityLens of item.quality
            )
        val response = client.invoke(request)
        when (response.status) {
            Status.NOT_FOUND -> null
            Status.OK -> priceLens(response)
            else -> error("Unexpected API response ${response.status}")
        }
    }
}

fun fakeValueElfRoutes(pricing: (ID<Item>, Quality) -> Price?) =
    routes(
        "/prices" bind Method.GET to { request ->
            try {
                val id = idLens(request)
                val quality = qualityLens(request)
                when (val price = pricing(id, quality)) {
                    null -> Response(Status.NOT_FOUND)
                    else -> Response(Status.OK).with(priceLens of price)
                }
            } catch (x: LensFailure) {
                Response(Status.BAD_REQUEST)
            }
        }
    )

val idLens: BiDiLens<Request, ID<Item>> = Query.nonEmptyString().map(
    nextIn = { string -> ID<Item>(string) ?: error("Unexpected failure to create id from $string") },
    nextOut = { id -> id.toString() }
).required("id")

val qualityLens: BiDiLens<Request, Quality> = Query.int().map(
    nextIn = { int -> Quality(int) ?: error("Failure to create id from $int") },
    nextOut = { quality -> quality.valueInt }
).required("quality")

val priceLens: BiDiBodyLens<Price?> = Body.nonEmptyString(ContentType.TEXT_PLAIN).map(
    nextIn = { string -> string.toLongOrNull()?.let { Price(it) }},
    nextOut = { price -> price?.pence?.toString() ?: error("Unexpected null price") }
).toLens()


