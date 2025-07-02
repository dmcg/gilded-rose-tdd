package com.gildedrose.browserTests

import com.gildedrose.DeleteItemsAcceptanceContract

class DeleteItemsBrowserTests : DeleteItemsAcceptanceContract(
    alison = PlaywrightActor(showBrowserTests)
) {
  override fun `delete non-existent item doesnt save stocklist`() {
        // we can't even try this in the browser
  }
}
