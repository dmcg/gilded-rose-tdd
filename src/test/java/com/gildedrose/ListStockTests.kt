package com.gildedrose

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ListStockTests {

    private val now = LocalDate.parse("2021-10-29")

    @Test
    fun `list stock`() {
        val stock = listOf(
            Item("banana", now.minusDays(1), 42u),
            Item("kumquat", now.plusDays(1), 101u)
        )
        val server = Server(stock) { now }
        val client = server.routes
        val response = client(Request(GET, "/"))
        assertEquals(expected, response.bodyString())
    }
}

@Language("HTML")
private val expected = """
    <html lang="en">
    <body>
    <table>
    <tr>
        <td>banana</td>
        <td>28 October 2021</td>
        <td>-1</td>
        <td>42</td>
    </tr>
    <tr>
        <td>kumquat</td>
        <td>30 October 2021</td>
        <td>1</td>
        <td>101</td>
    </tr>

    </table>
    </body>
    </html>
    """.trimIndent()
