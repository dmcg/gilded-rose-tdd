package com.gildedrose

import org.http4k.core.HttpHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer


class Server(
    routes: HttpHandler
) {
    private val http4kServer = routes.asServer(Undertow(8080))

    fun start() {
        http4kServer.start()
    }
}
