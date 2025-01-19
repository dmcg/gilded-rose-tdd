package com.gildedrose.testing

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

// Nasty little hack to initialise Jackson on test class loading rather than during tests
class InitJackson {
    companion object {
        init {
            thread {
                ObjectMapper()
            }
       }
    }

    @Test fun dummy() {
    }
}
