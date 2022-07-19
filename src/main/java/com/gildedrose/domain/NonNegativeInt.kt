package com.gildedrose.domain

@JvmInline
value class NonNegativeInt
private constructor(val value: Int)  {
    companion object {
        operator fun invoke(value: Int): NonNegativeInt? =
            if (value >= 0) NonNegativeInt(value)
            else null
    }

    init {
        require(value >= 0)
    }

    override fun toString() = value.toString()
}
