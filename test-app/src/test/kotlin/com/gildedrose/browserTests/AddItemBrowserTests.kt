package com.gildedrose.browserTests

import com.gildedrose.AddItemAcceptanceContract
import com.gildedrose.BrowserActor
import com.gildedrose.Fixture
import com.gildedrose.domain.Item
import com.gildedrose.routes
import java.time.format.DateTimeFormatter

private const val showRunning = showBrowserTests

class TestBrowserActor : BrowserActor() {
    override fun adds(fixture: Fixture, item: Item) {
        addWithPlaywright(fixture, item)
    }

    override fun deletes(fixture: Fixture, items: Set<Item>) {
        deleteWithPlaywright(fixture, items)
    }
}

class AddItemBrowserTests : AddItemAcceptanceContract(
    actor = TestBrowserActor()
)

private fun addWithPlaywright(fixture: Fixture, newItem: Item) {
    runWithPlaywright(
        fixture.app.routes,
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

private fun deleteWithPlaywright(fixture: Fixture, toDelete: Set<Item>) {
    runWithPlaywright(
        fixture.app.routes,
        launchOptions = launchOptions(showRunning)
    ) {
        toDelete.forEach {
            checkBoxNamed(it.id.toString()).click()
        }
        waitingForHtmx {
            acceptNextDialog()
            submitButtonNamed("Delete").click()
        }
        checkReloadsTheSame()
    }
}
