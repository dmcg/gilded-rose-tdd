package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data

@Data
@JvmInline
value class NonNegativeInt
private constructor(val value: Int)  {
    companion object {
        @Calculation
        operator fun invoke(value: Int): NonNegativeInt? =
            if (value >= 0) NonNegativeInt(value)
            else null
    }

    init {
        require(value >= 0)
    }

    @Calculation override fun toString() = value.toString()
    @Calculation operator fun minus(rhs: NonNegativeInt): Int = this.value - rhs.value
    @Calculation operator fun minus(rhs: Int): Int = this.value - rhs
    @Calculation operator fun unaryMinus(): Int = -this.value
    @Calculation operator fun plus(rhs: Int): Int = value + rhs
}
