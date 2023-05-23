package com.gildedrose.persistence

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class RememberToCloseJooqItemsClass {

    @Test
    fun `JooqItems is not open when DualItemsTests doesn't exist`() {
        val dualItemsTestsExists = try {
            Class.forName("com.gildedrose.persistence.DualItemsTests")
            true
        } catch (x: ClassNotFoundException) {
            false
        }
        if (!dualItemsTestsExists)
            assertFalse(
                JooqItems::class.isOpen,
                "Remember to remove the open modifier on JooqItems now it isn't needed by the tests"
            )
    }
}
