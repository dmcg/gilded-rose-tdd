package com.gildedrose

import com.gildedrose.domain.Item
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.time.format.DateTimeFormatter

private const val showRunning = false

@EnabledIfSystemProperty(named = "run-browser-tests", matches = "true")
class AddItemBrowserTests : AddItemAcceptanceContract(
    doAdd = ::addWithPlaywright
)

private fun addWithPlaywright(app: App<*>, newItem: Item) {
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
