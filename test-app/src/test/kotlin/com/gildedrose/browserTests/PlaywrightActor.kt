package com.gildedrose.browserTests

import com.gildedrose.Actor
import com.gildedrose.Fixture
import com.gildedrose.domain.Item
import com.gildedrose.routes
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import java.time.format.DateTimeFormatter

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

    override fun add(fixture: Fixture, item: Item) {
        runWithPlaywright(
            fixture.app.routes,
            launchOptions = launchOptions(showRunning)
        ) {
            inputNamed("new-itemId").fill(item.id.toString())
            inputNamed("new-itemName").fill(item.name.toString())
            item.sellByDate?.let {
                inputNamed("new-itemSellBy")
                    .pressSequentially(it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
            }
            inputNamed("new-itemQuality").fill(item.quality.toString())

            waitingForHtmx {
                submitButtonNamed("Add").click()
            }
            checkReloadsTheSame()
        }
    }
}

private fun Page.checkBoxFor(
    item: Item
): Locator = checkBoxNamed(item.id.toString())
