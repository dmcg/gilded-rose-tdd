package com.gildedrose

import com.gildedrose.testing.Given
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test

class EditItemsDirectlyTests : EditItemAcceptanceContract(DirectActor()) {
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

}
