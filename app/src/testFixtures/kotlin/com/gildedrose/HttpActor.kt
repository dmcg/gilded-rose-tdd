package com.gildedrose

import com.gildedrose.domain.Item

/**
 * Actor implementation that interacts with the system via HTTP.
 * This is a placeholder class that will be implemented in the test source set.
 */
abstract class HttpActor : Actor() {
    /**
     * Indicates whether to use HTMX headers in requests.
     */
    abstract val withHtmx: Boolean
}

/**
 * Actor implementation that interacts with the system via HTTP with HTMX headers.
 */
open class HtmxHttpActor : HttpActor() {
    override val withHtmx: Boolean = true

    override fun adds(fixture: Fixture, item: Item) {
        // Implementation will be provided in the test source set
    }

    override fun deletes(fixture: Fixture, items: Set<Item>) {
        // Implementation will be provided in the test source set
    }
}

/**
 * Actor implementation that interacts with the system via HTTP without HTMX headers.
 */
open class NoHtmxHttpActor : HttpActor() {
    override val withHtmx: Boolean = false

    override fun adds(fixture: Fixture, item: Item) {
        // Implementation will be provided in the test source set
    }

    override fun deletes(fixture: Fixture, items: Set<Item>) {
        // Implementation will be provided in the test source set
    }
}
