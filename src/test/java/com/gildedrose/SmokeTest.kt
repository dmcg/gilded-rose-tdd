package com.gildedrose

import com.natpryce.hamkrest.assertion.assertThat
import dbConfig
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class SmokeTest {

    @Test fun `run the app and list stock`() {
        val app = App(dbConfig = dbConfig)
        val response = app.routes(Request(GET, "/"))
        assertThat(response, hasStatus(OK))
        app.routes(Request(GET, "/"))
    }
}
