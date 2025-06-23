package com.gildedrose.browserTests

import com.gildedrose.AddItemAcceptanceContract
import com.gildedrose.Fixture
import com.gildedrose.domain.Item
import com.gildedrose.routes
import java.time.format.DateTimeFormatter

private const val showRunning = showBrowserTests

class AddItemBrowserTests : AddItemAcceptanceContract(Fixture::addWithPlaywright)

private fun Fixture.addWithPlaywright(newItem: Item) {
    runWithPlaywright(
        app.routes,
        launchOptions = launchOptions(showRunning)
    ) {
        inputNamed("new-itemId").fill(newItem.id.toString())
        inputNamed("new-itemName").fill(newItem.name.toString())
        newItem.sellByDate?.let {
            inputNamed("new-itemSellBy")
                .pressSequentially(it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
        }
        inputNamed("new-itemQuality").fill(newItem.quality.toString())

        waitingForHtmx {
            submitButtonNamed("Add").click()
        }
        checkReloadsTheSame()
    }
}
