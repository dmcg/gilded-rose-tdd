package com.gildedrose.http

import org.http4k.core.HttpHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer


fun serverFor(
    port: Int,
    routes: HttpHandler
) = routes.asServer(Undertow(port))
