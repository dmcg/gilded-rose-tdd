package com.gildedrose.competition

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.message
import strikt.assertions.size

class PropertySetTests {

    @Test
    fun `string value`() {
        val propertySet: PropertySet = mapOf("key" to "value")
        expectThat(propertySet.valueOf<String>("key"))
            .isEqualTo("value")
        expectThat(propertySet.valueOf<String?>("key"))
            .isEqualTo("value")
    }

    @Test
    fun `int value`() {
        val propertySet: PropertySet = mapOf("key" to 42)
        expectThat(propertySet.valueOf<Int>("key"))
            .isEqualTo(42)
        expectThat(propertySet.valueOf<Int?>("key"))
            .isEqualTo(42)
    }

    @Test
    fun `list value`() {
        val propertySet: PropertySet = mapOf("key" to listOf(42, 99))
        expectThat(propertySet.valueOf<List<Int>>("key"))
            .isEqualTo(listOf(42, 99))
    }

    @Test
    fun `wrong type throws`() {
        val propertySet: PropertySet = mapOf("key" to "value")
        expectThrows<NoSuchElementException> {
            propertySet.valueOf<Int>("key")
        }.message.isEqualTo(
            "Value for key <key> is not a class kotlin.Int"
        )
    }

    @Test
    fun `wrong list type doesnt throw`() {
        val propertySet: PropertySet = mapOf("key" to listOf(42, 99))
        expectThat(propertySet.valueOf<List<String>>("key"))
            .size.isEqualTo(2)
    }

    @Test
    fun `no key returns null for nullable property`() {
        val propertySet: PropertySet = emptyMap()
        expectThat(propertySet.valueOf<String?>("key")).isNull()
    }

    @Test
    fun `no key throws for non-null property`() {
        val propertySet: PropertySet = emptyMap()
        expectThrows<NoSuchElementException> {
            propertySet.valueOf<String>("any")
        }.message.isEqualTo(
            "Key <any> is missing in the map"
        )
    }

    @Test
    fun `null value returns null for nullable property`() {
        val propertySet: PropertySet = mapOf("key" to null)
        expectThat(propertySet.valueOf<String?>("key")).isNull()
    }

    @Test
    fun `null value throws for non-null property`() {
        val propertySet: PropertySet = mapOf("key" to null)
        expectThrows<NoSuchElementException> {
            propertySet.valueOf<String>("key")
        }.message.isEqualTo(
            "Value for key <key> is null"
        )
    }
}
