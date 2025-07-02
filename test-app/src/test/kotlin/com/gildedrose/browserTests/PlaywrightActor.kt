package com.gildedrose.browserTests

import com.gildedrose.Actor
import com.gildedrose.Fixture
import com.gildedrose.domain.Item
import com.gildedrose.routes
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class PlaywrightActor(private val showRunning: Boolean) : Actor() {
    override fun delete(fixture: Fixture, items: Set<Item>) {
        runWithPlaywright(
            fixture.app.routes,
            launchOptions = launchOptions(showRunning)
        ) {
            items.forEach {
                checkBoxFor(it).click()
            }
            waitingForHtmx {
                acceptNextDialog()
                submitButtonNamed("Delete").click()
            }

            checkReloadsTheSame()
        }
    }
}

private fun Page.checkBoxFor(
    item: Item
): Locator = checkBoxNamed(item.id.toString())
