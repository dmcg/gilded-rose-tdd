package com.gildedrose

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import routesFor
import java.io.File
import java.time.Instant
import java.time.LocalDate

@ExtendWith(ApprovalTest::class)
class ListStockTests {

    @TempDir
    lateinit var dir: File
    private val stockFile by lazy { dir.resolve("stock.tsv") }
    private val now = LocalDate.parse("2021-10-29")

    @Test
    fun `list stock`(approver: Approver) {
        StockList(
            Instant.now(),
            listOf(
                Item("banana", now.minusDays(1), 42u),
                Item("kumquat", now.plusDays(1), 101u)
            )
        ).saveTo(stockFile)
        val routes = routesFor(stockFile) { now }
        approver.assertApproved(routes(Request(GET, "/")), OK)
    }
}
