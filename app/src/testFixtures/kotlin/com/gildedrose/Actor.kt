package com.gildedrose

import com.gildedrose.domain.Item

/**
 * Abstract Actor class that defines methods for interacting with the system.
 * Different implementations can interact with the system in different ways (directly, via HTTP, via UI).
 */
abstract class Actor {
    /**
     * Adds an item to the system.
     */
    abstract fun adds(fixture: Fixture, item: Item)

    /**
     * Deletes items from the system.
     */
    abstract fun deletes(fixture: Fixture, items: Set<Item>)
}
