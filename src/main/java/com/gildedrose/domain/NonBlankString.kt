package com.gildedrose.domain

@JvmInline
value class NonBlankString(val value: String) : CharSequence by value {

    init {
        require(value.isNotBlank())
    }

    override fun toString() = value
}

fun NonBlankString(value: String): NonBlankString? =
    if (value.isNotBlank()) NonBlankString(value)
    else null
