package com.gildedrose

import com.gildedrose.domain.StockList
import com.gildedrose.testing.Given
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant

abstract class DeleteItemsAcceptanceContract(
    val actor: Actor,
) {
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")

    @Test
    fun `delete items`() {
        Given(
            aSampleFixture(
                stockListLastModified = lastModified,
                now = sameDayAsLastModified
            )
        ).When {
            actor.deletes(
                this,
                setOf(
                    originalStockList[0],
                    originalStockList[2],
                )
            )
        }.Then {
            checkCurrentSockListIs(
                StockList(now, listOf(originalStockList[1]))
            )
        }
    }

    @Test
    fun `delete no items doesnt save stocklist`() {
        Given(
            aSampleFixture(
                stockListLastModified = lastModified,
                now = sameDayAsLastModified
            )
        ).When {
            actor.deletes(this, emptySet())
        }.Then {
            checkCurrentSockListIs(originalStockList)
        }
    }

    @Test
    open fun `delete non-existent item doesnt save stocklist`() {
        Given(
            aSampleFixture(
                stockListLastModified = lastModified,
                now = sameDayAsLastModified
            )
        ).When {
            actor.deletes(this, setOf(item("no-such", "not in stock", null, 0)))
        }.Then {
            checkCurrentSockListIs(originalStockList)
        }
    }
}
