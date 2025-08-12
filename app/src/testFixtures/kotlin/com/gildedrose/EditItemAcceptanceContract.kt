package com.gildedrose

import com.gildedrose.domain.NonBlankString
import com.gildedrose.domain.StockList
import com.gildedrose.testing.Given
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant.parse as t

abstract class EditItemAcceptanceContract(
    val alison: Actor
) {
    private val stockListLastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

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
    fun `edit non-existent item doesnt save stocklist`() {
        val notReallyEdited = item("no-such", "not in stock", null, 0)
        Given(
            aSampleFixture(stockListLastModified, now = sameDayAsLastModified)
        ).When {
            alison.edits(notReallyEdited)
        }.Then {
            checkCurrentSockListIs(originalStockList)
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
