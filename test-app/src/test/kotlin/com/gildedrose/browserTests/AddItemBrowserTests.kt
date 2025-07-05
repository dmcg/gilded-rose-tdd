package com.gildedrose.browserTests

import com.gildedrose.AddItemAcceptanceContract

class AddItemBrowserTests : AddItemAcceptanceContract(
    alison = PlaywrightActor(showBrowserTests)
)
