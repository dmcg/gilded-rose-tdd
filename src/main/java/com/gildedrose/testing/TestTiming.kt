package com.gildedrose.testing

import java.time.Instant

interface TestTiming {
    companion object {
        private var INSTANCE: TestTiming? = null

        fun init(testTiming: TestTiming) {
            if (INSTANCE != null) error("Another TestTiming exists")
            INSTANCE = testTiming
        }

        fun event(name: String, now: Instant = Instant.now()) {
            INSTANCE?.event(name, now) ?: error("No TestTiming exists")
        }
    }
    fun event(name: String, now: Instant = Instant.now())
}
