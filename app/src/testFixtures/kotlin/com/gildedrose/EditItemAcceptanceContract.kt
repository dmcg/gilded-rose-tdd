package com.gildedrose

import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.StockList
import com.gildedrose.testing.Given
import org.junit.jupiter.api.Test
import java.time.Instant.parse as t

abstract class EditItemAcceptanceContract(
    val alison: Actor
) {
    protected val stockListLastModified = t("2022-02-09T12:00:00Z")
    protected val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    @Test
    fun `edit existing item`() {
        Given(
            aSampleFixture(stockListLastModified, now = sameDayAsLastModified)
        ).When {
            val edited = originalStockList.first().copy(name = NonBlankString("ripe banana")!!)
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
