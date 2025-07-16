package com.gildedrose.browserTests

import com.gildedrose.DeleteItemsAcceptanceContract

private const val showRunning = showBrowserTests

class DeleteItemsBrowserTests : DeleteItemsAcceptanceContract(
    actor = TestBrowserActor()
) {
    override fun `delete non-existent item doesnt save stocklist`() {
        // we can't even try this in the browser
    }
}
