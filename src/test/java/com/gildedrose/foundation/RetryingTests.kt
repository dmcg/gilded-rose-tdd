package com.gildedrose.foundation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RetryingTests {

    @Test
    fun `returns value if no exception`() {
        val wrapped: (String) -> String = succeedAfter(0)
        val retrying: (String) -> String = retry(function = wrapped)
        assertEquals("banana", retrying("banana"))
    }

    @Test
    fun `retries if exception`() {
        val wrapped = succeedAfter(1)
        val retrying = retry(function = wrapped)
        assertEquals("banana", retrying("banana"))
    }

    @Test
    fun `retries if more than one exception`() {
        val wrapped = succeedAfter(2)
        val retrying = retry(retries = 2, function = wrapped)
        assertEquals("banana", retrying("banana"))
    }

    @Test
    fun `doesnt retry if retries is 0`() {
        val wrapped = succeedAfter(1)
        val retrying = retry(retries = 0, function = wrapped)
        assertThrows<IllegalStateException> {  retrying("banana") }
    }

    @Test
    fun `leaks exception if more errors than retries`() {
        val wrapped = succeedAfter(2)
        val retrying = retry(retries = 1, function = wrapped)
        assertThrows<IllegalStateException> {  retrying("banana") }
    }

    @Test
    fun `reports exceptions`() {
        val reported = mutableListOf<Exception>()
        val wrapped = succeedAfter(2)
        val retrying = retry(retries = 2, reported::add, function = wrapped)
        assertEquals("banana", retrying("banana"))
        assertEquals(listOf("deliberate", "deliberate"), reported.map(Exception::message))
    }

    private fun succeedAfter(exceptionCount: Int): (String) -> String {
        var countdown = exceptionCount
        val wrapped: (String) -> String = { if (countdown-- == 0) it else error("deliberate") }
        return wrapped
    }

}

fun <T, R> retry(
    retries: Int = 1,
    reporter: (Exception) -> Unit = {},
    function: (T) -> R
): (T) -> R {
    return fun(it: T): R {
        var countdown = retries
        while (true) {
            try {
                return function(it)
            } catch (x: Exception) {
                if (countdown-- == 0) throw x
                else reporter(x)
            }
        }
    }
}
