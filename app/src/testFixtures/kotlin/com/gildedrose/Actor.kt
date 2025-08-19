package com.gildedrose

import com.gildedrose.domain.Item

abstract class Actor {
    abstract fun delete(fixture: Fixture, items: Set<Item>)
    abstract fun add(fixture: Fixture, item: Item)
    open fun edit(fixture: Fixture, item: Item) {
        throw UnsupportedOperationException("Edit not implemented for this Actor")
    }
    open fun editAndThenCancel(fixture: Fixture, item: Item) {
        throw UnsupportedOperationException("EditsButThenCancels not implemented for this Actor")
    }
}
