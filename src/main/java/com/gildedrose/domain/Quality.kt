package com.gildedrose.domain

import com.gildedrose.theory.Calculation
import com.gildedrose.theory.Data

@Data
@JvmInline
value class Quality(
    private val value: NonNegativeInt
) {
    @Calculation
    val valueInt get() = value.value

    companion object {
        val ZERO: Quality = Quality(0)!!

        @Calculation
        operator fun invoke(value: Int): Quality? =
            NonNegativeInt(value)?.let { Quality(it) }
    }

    @Calculation override fun toString() = value.toString()

    @Calculation operator fun minus(rhs: Quality): Int = this.value - rhs.value
    @Calculation operator fun unaryMinus(): Int = -this.value

    @Calculation
    operator fun plus(rhs: Int): Quality {
        val qualityCap = value.value.coerceAtLeast(50)
        return Quality(
            (value + rhs).coerceIn(0, qualityCap)
        ) ?: error("tried to create a negative int")
    }

    @Calculation operator fun minus(rhs: Int): Quality = this + -rhs
}
