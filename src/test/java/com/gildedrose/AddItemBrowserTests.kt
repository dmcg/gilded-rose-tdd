package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.foundation.IO
import com.gildedrose.testing.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.format.DateTimeFormatter

private const val showRunning = false

context(IO)
@ExtendWith(IOResolver::class)
class AddItemBrowserTests : AddItemAcceptanceContract(
    doAdd = ::addWithPlaywright
)

private fun addWithPlaywright(app: App, newItem: Item) {
    runWithPlaywright(
        app.routes,
        launchOptions = launchOptions(showRunning)
    ) {
        inputNamed("new-itemId").type(newItem.id.toString())
        inputNamed("new-itemName").type(newItem.name.toString())
        newItem.sellByDate?.let {
            inputNamed("new-itemSellBy")
                .type(it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
        }
        inputNamed("new-itemQuality").type(newItem.quality.toString())

        waitingForHtmx {
            submitButtonNamed("Add").click()
        }
        checkReloadsTheSame()
    }
}
