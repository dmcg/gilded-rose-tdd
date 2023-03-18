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
    operator fun minus(rhs: NonNegativeInt): Int = this.value - rhs.value
    operator fun minus(rhs: Int): Int = this.value - rhs
    operator fun unaryMinus(): Int = -this.value
    operator fun plus(rhs: Int): Int = value + rhs
}
