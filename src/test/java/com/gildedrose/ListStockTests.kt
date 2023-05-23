package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.runIO
import com.gildedrose.persistence.InMemoryItems
import com.gildedrose.persistence.Items
import com.gildedrose.persistence.NoTX
import com.gildedrose.persistence.StockListLoadingError
import com.gildedrose.testing.IOResolver
import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

@ExtendWith(IOResolver::class)
@ExtendWith(ApprovalTest::class)
class ListStockTests {

    companion object {
        private val lastModified = t("2022-02-09T12:00:00Z")
        private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

        private val stockList = StockList(
            lastModified = lastModified,
            items = listOf(
                item("banana", localDate("2022-02-08"), 42),
                item("kumquat", localDate("2022-02-10"), 101),
                item("undated", null, 50)
            )
        )
        private val valueElfPricing = { id: ID<Item>, quality: Quality ->
            when (id) {
                stockList[0].id -> Price(666)
                stockList[1].id -> null
                stockList[2].id -> Price(999)
                else -> error("Unexpected item for pricing")
            }
        }
        private val expectedPricedStockList = stockList.withItems(
            stockList[0].withPrice(Price(666)),
            stockList[1].withPrice(null),
            stockList[2].withPrice(Price(999))
        )
    }

    context(IO)
    @Test
    fun `list stock`(approver: Approver) {
        val items = InMemoryItems().apply {
            runIO {
                inTransaction { save(stockList) }
            }
        }
        val app = App(
            items = items,
            pricing = { item -> valueElfPricing(item.id, item.quality) },
            clock = { sameDayAsLastModified }
        )
        assertEquals(
            Success(expectedPricedStockList),
            app.loadStockList()
        )
        approver.assertApproved(app.routes(Request(GET, "/")), OK)
    }

    context(IO)
    @Test
    fun `reports errors`() {
        val expectedError = StockListLoadingError.BlankName("B1\t\t2022-02-08\t42")

        val itemsThatFails = object : Items<NoTX> {
            override fun <R> inTransaction(block: context(NoTX) () -> R) = block(NoTX)


            context(IO, NoTX) override fun load(): Result<StockList, StockListLoadingError> {
                return Failure(expectedError)
            }

            context(IO, NoTX) override fun save(stockList: StockList): Result<StockList, StockListLoadingError.IOError> {
                throw NotImplementedError()
            }

        }
        val events: MutableList<Any> = mutableListOf()

        val app = App(
            items = itemsThatFails,
            pricing = { item -> valueElfPricing(item.id, item.quality) },
            clock = { sameDayAsLastModified },
            analytics = events::add
        )

        assertEquals(
            Failure(expectedError),
            app.loadStockList()
        )
        assertThat(app.routes(Request(GET, "/")), hasStatus(INTERNAL_SERVER_ERROR))
        assertEquals(
            expectedError,
            events.first()
        )
    }
}
