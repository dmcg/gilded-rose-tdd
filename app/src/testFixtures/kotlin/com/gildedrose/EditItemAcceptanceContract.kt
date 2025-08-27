package com.gildedrose

import com.gildedrose.domain.*
import com.gildedrose.testing.Given
import com.gildedrose.testing.item
import com.gildedrose.testing.withPriceResult
import org.junit.jupiter.api.Test
import java.time.LocalDate.parse
import kotlin.test.assertNull
import java.time.Instant.parse as t

abstract class EditItemAcceptanceContract(
    val alison: Actor,
) {
    protected val stockListLastModified = t("2022-02-09T12:00:00Z")
    protected val sameDayAsLastModified = t("2022-02-09T23:59:59Z")
    protected val aSampleFixture = Fixture(
        PricedStockList(
            stockListLastModified,
            listOf(
                item("banana", parse("2022-02-08"), 42).withPriceResult(Price(666)),
                item("kumquat", parse("2022-02-10"), 101).withPriceResult(null),
                item("undated", null, 50).withPriceResult(Price(999))
            )
        ),
        now = sameDayAsLastModified
    )

    @Test
    fun `edit existing item`() {
        Given(
            aSampleFixture
        ).When {
            val edited = originalStockList.first().copy(
                name = NonBlankString("ripe banana")!!,
                quality = Quality(43)!!,
                sellByDate = parse("2022-02-09"))
            alison.edits(edited)
            edited
        }.Then { edited ->
            val expectedItems = listOf(
                edited,
                originalStockList[1],
                originalStockList[2],
            )
            checkCurrentSockListIs(
                StockList(now, expectedItems)
            )
        }
    }

    @Test
    fun `edit existing item removing sell by date`() {
        Given(
            aSampleFixture
        ).When {
            val edited = originalStockList.first().copy(sellByDate = null)
            alison.edits(edited)
            edited
        }.Then { edited ->
            val expectedItems = listOf(
                edited,
                originalStockList[1],
                originalStockList[2],
            )
            checkCurrentSockListIs(
                StockList(now, expectedItems)
            )
        }
    }

    @Test
    fun `edit existing item adding sell by date`() {
        Given(
            aSampleFixture
        ).When {
            val edited = originalStockList[2]
                .also { assertNull(it.sellByDate) }
                .copy(sellByDate = parse("2022-02-09"))
            alison.edits(edited)
            edited
        }.Then { edited ->
            val expectedItems = listOf(
                originalStockList[0],
                originalStockList[1],
                edited,
            )
            checkCurrentSockListIs(
                StockList(now, expectedItems)
            )
        }
    }

    @Test
    fun `edit unchanged item doesnt save stocklist`() {
        Given(
            aSampleFixture(stockListLastModified, now = sameDayAsLastModified)
        ).When {
            val unchangedItem = originalStockList.items[1]
            alison.edits(unchangedItem)
        }.Then {
            checkCurrentSockListIs(originalStockList)
        }
    }
}
