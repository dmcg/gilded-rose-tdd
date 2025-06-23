package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.testing.Given
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant.parse as t
import java.time.LocalDate.parse as localDate

abstract class AddItemAcceptanceContract(
    private val add: (Fixture).(Item) -> Unit,
) {
    private val stockListLastModified = t("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = t("2022-02-09T23:59:59Z")

    @Test
    fun `add item`() {
        val newItem = item("new-id", "new name", localDate("2023-07-23"), 99)
        val timeOfRequest = sameDayAsLastModified
        Given(
            aSampleFixture(stockListLastModified, now = timeOfRequest)
        ).When {
            add(newItem)
        }.Then {
            checkCurrentSockListIs(
                StockList(timeOfRequest, originalStockList + newItem)
            )
        }
    }

    @Test
    fun `add item with no date`() {
        val newItem = item("new-id", "new name", null, 99)
        Given(
            aSampleFixture(stockListLastModified, now = sameDayAsLastModified)
        ).When {
            add(newItem)
        }.Then {
            checkCurrentSockListIs(
                StockList(sameDayAsLastModified, originalStockList + newItem)
            )
        }
    }
}
