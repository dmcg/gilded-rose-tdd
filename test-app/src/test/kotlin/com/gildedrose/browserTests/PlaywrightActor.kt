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
                buttonNamed("Delete").click()
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
                buttonNamed("Add").click()
            }
            checkReloadsTheSame()
        }
    }

    override fun edit(fixture: Fixture, item: Item) {
        performEditOperationAnd(fixture, item) {
            buttonNamed("Save").click()
        }
    }

    override fun editAndThenCancel(fixture: Fixture, item: Item) {
        performEditOperationAnd(fixture, item) {
            locator("a:has-text('Cancel')").click()
        }
    }

    private fun performEditOperationAnd(fixture: Fixture, item: Item, action: Page.() -> Unit) {
        runWithPlaywright(
            fixture.app.routes,
            launchOptions = launchOptions(showRunning)
        ) {
            waitingForHtmx {
                editButtonFor(item).click()
            }

            // Fill in the edit form fields
            inputNamed("edit-itemName").fill(item.name.toString())
            item.sellByDate?.let {
                inputNamed("edit-itemSellBy").clear()
                inputNamed("edit-itemSellBy")
                    .pressSequentially(it.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
            } ?: inputNamed("edit-itemSellBy").clear()
            inputNamed("edit-itemQuality").fill(item.quality.toString())

            // Execute the specific action (Save or Cancel)
            waitingForHtmx {
                action()
            }
            checkReloadsTheSame()
        }
    }
}

private fun Page.checkBoxFor(
    item: Item,
): Locator = checkBoxNamed(item.id.toString())

private fun Page.editButtonFor(
    item: Item,
): Locator = locator("tr:has-text('${item.id}') a:has-text('Edit')")
