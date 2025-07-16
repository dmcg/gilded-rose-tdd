package com.gildedrose

class DeleteItemsHttpTests : DeleteItemsAcceptanceContract(
    actor = TestHtmxHttpActor()
)

class DeleteItemsHttpWithNoHtmxTests : DeleteItemsAcceptanceContract(
    actor = TestNoHtmxHttpActor()
)
