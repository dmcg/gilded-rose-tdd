package com.gildedrose.browserTests

import com.gildedrose.EditItemAcceptanceContract
import com.gildedrose.aSampleFixture
import com.gildedrose.domain.NonBlankString
import com.gildedrose.testing.Given
import org.junit.jupiter.api.Test


class EditItemsBrowserTests : EditItemAcceptanceContract(
    alison = PlaywrightActor(showBrowserTests)
) {
    @Test
    fun `edit but cancel existing item`() {
        Given(
            aSampleFixture(stockListLastModified, now = sameDayAsLastModified)
        ).When {
            val edited = originalStockList.first().copy(name = NonBlankString("ripe banana")!!)
            alison.editsButThenCancels(edited)
        }.Then {
            checkCurrentSockListIs(originalStockList)
        }
    }

}
