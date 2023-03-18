package com.gildedrose.domain

@JvmInline
value class ID<@Suppress("unused") T>(val value: NonBlankString) {

    companion object {
        operator fun <T> invoke(value: String): ID<T>? =
            NonBlankString(value)?.let { ID<T>(it) }
    }

    override fun toString() = value.toString()
}
