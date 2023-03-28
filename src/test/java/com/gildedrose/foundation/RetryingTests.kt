package com.gildedrose.foundation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RetryingTests {

    @Test
    fun `returns value if no exception - raw`() {
        val wrapped: (String) -> String = succeedAfter(0)
        val retrying: (String) -> String = wrapped.wrappedWith(
            retry()
        )
        assertEquals("banana", retrying("banana"))
    }

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

    private fun succeedAfter(exceptionCount: Int): (String) -> String =
        succeedAfter(exceptionCount, raiseError = { error("deliberate") } ) { it }
}

fun <T, R> succeedAfter(exceptionCount: Int, raiseError: () -> Nothing, f: (T) -> R): (T) -> R {
    var countdown = exceptionCount
    return { if (countdown-- == 0) f(it) else raiseError() }
}
