package com.gildedrose.foundation

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message
import strikt.assertions.size

class PropertySetTests {

    @Test
    fun `empty map has no keys`() {
        val propertySet: PropertySet = emptyMap()
        expectThrows<NoSuchElementException> {
            propertySet.required("any")
        }.message.isEqualTo(
            "Key any is missing in the map."
        )
    }

    @Test
    fun `null key throws`() {
        val propertySet: PropertySet = mapOf("key" to null)
        expectThrows<IllegalStateException> {
            propertySet.required("key")
        }.message.isEqualTo(
            "Key <key> is null"
        )
    }

    @Test
    fun `string value`() {
        val propertySet: PropertySet = mapOf("key" to "value")
        expectThat(propertySet.required<String>("key"))
            .isEqualTo("value")
    }

    @Test
    fun `int value`() {
        val propertySet: PropertySet = mapOf("key" to 42)
        expectThat(propertySet.required<Int>("key"))
            .isEqualTo(42)
    }

    @Test
    fun `list value`() {
        val propertySet: PropertySet = mapOf("key" to listOf(42, 99))
        expectThat(propertySet.required<List<Int>>("key"))
            .isEqualTo(listOf(42, 99))
    }

    @Test
    fun `wrong type throws`() {
        val propertySet: PropertySet = mapOf("key" to "value")
        expectThrows<IllegalStateException> {
            propertySet.required<Int>("key")
        }.message.isEqualTo(
            "Value for key <key> is not a class kotlin.Int"
        )
    }

    @Test
    fun `wrong list type doesnt throw`() {
        val propertySet: PropertySet = mapOf("key" to listOf(42, 99))
        expectThat(propertySet.required<List<String>>("key"))
            .size.isEqualTo(2)
    }

    @Test
    fun `path of two keys`() {
        val propertySet: PropertySet = mapOf(
            "key" to
                mapOf("key2" to "value")
        )
        expectThat(propertySet.required<String>("key", "key2"))
            .isEqualTo("value")

        expectThrows<NoSuchElementException> {
            propertySet.required("no-such", "key2")
        }.message.isEqualTo(
            "Key no-such is missing in the map."
        )
        expectThrows<NoSuchElementException> {
            propertySet.required("key", "no-such")
        }.message.isEqualTo(
            "Key no-such is missing in the map."
        )
    }

    @Test
    fun `path of three keys`() {
        val propertySet: PropertySet = mapOf(
            "key" to
                mapOf(
                    "key2" to mapOf("key3" to "value")
                )
        )
        expectThat(propertySet.required<String>("key", "key2", "key3"))
            .isEqualTo("value")
    }

    @Test
    fun `path of no keys`() {
        val propertySet: PropertySet = mapOf(
            "key" to
                mapOf(
                    "key2" to mapOf("key3" to "value")
                )
        )
        expectThrows<IllegalStateException> {
            (propertySet.required<String>(emptyList()))
        }.message.isEqualTo("no keys supplied")
    }

}
