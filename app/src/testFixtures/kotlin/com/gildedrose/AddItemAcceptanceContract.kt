package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.testing.item
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

abstract class AddItemAcceptanceContract(
    private val doAdd: (App<*>, Item) -> Unit
) {
    private val lastModified = Instant.parse("2022-02-09T12:00:00Z")
    private val sameDayAsLastModified = Instant.parse("2022-02-09T23:59:59Z")

    fun App<*>.add(item: Item) = doAdd(this, item)

    @Test
    fun `add item`() {
        val fixture = aSampleFixture(lastModified)
        val newItem = item("new-id", "new name", LocalDate.parse("2023-07-23"), 99)
        Given(fixture, now = sameDayAsLastModified)
            .When {
                add(newItem)
            }
            .Then {
                fixture.checkStockListIs(
                    StockList(
                        sameDayAsLastModified,
                        fixture.originalStockList + newItem
                    )
                )
            }
    }

    @Test
    fun `add item TOO`() {
        val fixture = aSampleFixture(lastModified)
        val newItem = item("new-id", "new name", LocalDate.parse("2023-07-23"), 99)
        Given(fixture, now = sameDayAsLastModified)
            .When {
                add(newItem)
            }
            .Then {
                fixture.checkStockListIs(
                    StockList(
                        sameDayAsLastModified,
                        fixture.originalStockList + newItem
                    )
                )
            }
    }

    @Test
    fun `add item with no date`() {
        val fixture = aSampleFixture(lastModified)
        val newItem = item("new-id", "new name", null, 99)
        Given(fixture, now = sameDayAsLastModified)
            .When {
                add(newItem)
            }
            .Then {
                fixture.checkStockListIs(
                    StockList(
                        sameDayAsLastModified,
                        fixture.originalStockList + newItem
                    )
                )
            }
    }
}

fun Given(fixture: Fixture, now: Instant): App<*> =
    fixture.createApp(now)

fun <T, R> T.When(block: T.(T) -> R): R = this.block(this)
fun <T, R> T.Then(block: T.(T) -> R): R = this.block(this)
