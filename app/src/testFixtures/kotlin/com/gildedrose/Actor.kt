package com.gildedrose

import com.gildedrose.domain.Item

abstract class Actor {
    abstract fun delete(fixture: Fixture, items: Set<Item>)
    abstract fun add(fixture: Fixture, item: Item)
}
