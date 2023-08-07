package com.gildedrose.domain

import arrow.core.raise.Raise

@JvmInline
value class NonBlankString
private constructor(val value: String) : CharSequence by value {
    companion object {
        operator fun invoke(value: String): NonBlankString? =
            if (value.isNotBlank()) NonBlankString(value)
            else null

        context(Raise<String>)
        operator fun invoke(value: String): NonBlankString =
            if (value.isNotBlank()) NonBlankString(value)
            else raise("String cannot be blank")
    }

    init {
        require(value.isNotBlank())
    }

    override fun toString() = value
}
