package com.gildedrose.browserTests

import com.gildedrose.EditItemAcceptanceContract

class EditItemsBrowserTests : EditItemAcceptanceContract(
    alison = PlaywrightActor(true)
)
