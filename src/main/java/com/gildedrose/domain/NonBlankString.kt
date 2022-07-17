package com.gildedrose.domain

@JvmInline
value class NonBlankString private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String): NonBlankString? =
            if (value.isNotBlank()) NonBlankString(value)
            else null
    }

    init {
        require(value.isNotBlank())
    }

    override fun toString() = value
}
