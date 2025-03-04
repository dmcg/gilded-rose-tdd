package com.gildedrose.browserTests

import com.gildedrose.App
import com.gildedrose.DeleteItemsAcceptanceContract
import com.gildedrose.domain.Item
import com.gildedrose.routes
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

private const val showRunning = showBrowserTests

class DeleteItemsBrowserTests : DeleteItemsAcceptanceContract(
    doDelete = ::deleteWithPlaywright
) {
    override fun `delete non-existent item doesnt save stocklist`() {
        // we can't even try this in the browser
    }
}

private fun deleteWithPlaywright(app: App<*>, toDelete: Set<Item>) {
    runWithPlaywright(
        app.routes,
        launchOptions = launchOptions(showRunning)
    ) {
        toDelete.forEach {
            checkBoxFor(it).click()
        }
        waitingForHtmx {
            acceptNextDialog()
            submitButtonNamed("Delete").click()
        }

        checkReloadsTheSame()
    }
}

private fun Page.checkBoxFor(
    item: Item
): Locator = checkBoxNamed(item.id.toString())
