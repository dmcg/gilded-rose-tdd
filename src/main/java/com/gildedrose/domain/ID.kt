package com.gildedrose.domain

import arrow.core.raise.Raise

@JvmInline
value class ID<@Suppress("unused") T>(val value: NonBlankString) {

    companion object {
        operator fun <T> invoke(value: String): ID<T>? =
            NonBlankString(value)?.let { ID<T>(it) }

        context(Raise<String>)
        operator fun <T> invoke(value: String): ID<T> = ID(NonBlankString(value))
    }

    override fun toString() = value.toString()

}
