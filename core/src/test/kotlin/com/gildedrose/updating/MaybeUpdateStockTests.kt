package com.gildedrose.updating

import com.gildedrose.domain.StockList
import com.gildedrose.testing.item
import com.gildedrose.testing.oct29
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals

class MaybeUpdateStockTests {
    private val someItems = listOf(
        item("banana", oct29, 42),
        item("kumquat", oct29, 101)
    )
    private val londonZone = ZoneId.of("Europe/London")

    @Test
    fun `loads stock but doesn't update if loaded same day as last modified`() {
        val initialStockList = StockList(Instant.parse("2021-02-09T00:00:00Z"), someItems)
        assertEquals(
            StockUpdateDecision.DoNothing(initialStockList),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-09T00:00:00Z"), londonZone)
        )
        assertEquals(
            StockUpdateDecision.DoNothing(initialStockList),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-09T23:59:59.99999Z"), londonZone)
        )
    }

    @Test
    fun `updates stock if last modified yesterday`() {
        val initialStockList = StockList(Instant.parse("2021-02-09T23:59:59Z"), someItems)
        val expectedItems = initialStockList.items.withQualityDecreasedBy(1)
        assertEquals(
            StockUpdateDecision.SaveUpdate(
                StockList(Instant.parse("2021-02-10T00:00:00Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-10T00:00:00Z"), londonZone)
        )
        assertEquals(
            StockUpdateDecision.SaveUpdate(
                StockList(Instant.parse("2021-02-10T23:59:59.9999Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-10T23:59:59.9999Z"), londonZone)
        )
    }

    @Test
    fun `updates stock by two days if last modified the day before yesterday`() {
        val initialStockList = StockList(Instant.parse("2021-02-09T23:59:59Z"), someItems)
        val expectedItems = someItems.withQualityDecreasedBy(2)
        assertEquals(
            StockUpdateDecision.SaveUpdate(
                StockList(Instant.parse("2021-02-11T00:00:00Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-11T00:00:00Z"), londonZone)
        )
        assertEquals(
            StockUpdateDecision.SaveUpdate(
                StockList(Instant.parse("2021-02-11T23:59:59.9999Z"), expectedItems)
            ),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-11T23:59:59.9999Z"), londonZone)
        )
    }

    @Test
    fun `does not update stock if modified tomorrow`() {
        val initialStockList = StockList(Instant.parse("2021-02-09T00:00:00Z"), someItems)
        assertEquals(
            StockUpdateDecision.DoNothing(initialStockList),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-08T00:00:00Z"), londonZone)
        )
        assertEquals(
            StockUpdateDecision.DoNothing(initialStockList),
            mayBeUpdate(initialStockList, Instant.parse("2021-02-08T23:59:59.99999Z"), londonZone)
        )
    }
}
