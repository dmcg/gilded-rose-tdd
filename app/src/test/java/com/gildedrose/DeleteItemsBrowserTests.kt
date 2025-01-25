package com.gildedrose

import com.gildedrose.domain.Item
import com.gildedrose.testing.*
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

private const val showRunning = true

@EnabledIfSystemProperty(named = "run-browser-tests", matches = "true")
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
